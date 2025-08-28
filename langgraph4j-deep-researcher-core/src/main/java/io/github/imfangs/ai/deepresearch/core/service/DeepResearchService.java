package io.github.imfangs.ai.deepresearch.core.service;

import io.github.imfangs.ai.deepresearch.api.dto.ResearchRequest;
import io.github.imfangs.ai.deepresearch.api.dto.ResearchResponse;
import io.github.imfangs.ai.deepresearch.api.state.ResearchState;
import io.github.imfangs.ai.deepresearch.config.ResearchModelConfig;
import io.github.imfangs.ai.deepresearch.config.ResearchConfig;
import io.github.imfangs.ai.deepresearch.core.graph.ResearchGraphBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Deep research service
 * 
 * @author imfangs
 */
@Slf4j
@Service
public class DeepResearchService {

    private final ResearchConfig researchConfig;
    private final ResearchGraphBuilder graphBuilder;

    @Autowired
    public DeepResearchService(ResearchConfig researchConfig, ResearchGraphBuilder graphBuilder) {
        this.researchConfig = researchConfig;
        this.graphBuilder = graphBuilder;
    }

    /**
     * Execute deep research
     * 
     * @param request Research request
     * @return Research response
     */
    public ResearchResponse executeResearch(ResearchRequest request) {
        // Generate request ID (if not provided)
        String requestId = request.getRequestId() != null ? 
                request.getRequestId() : UUID.randomUUID().toString();

        log.info("🚀 Starting deep research execution, request ID: {}, research topic: {}", requestId, request.getResearchTopic());

        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // Build ChatModel
            ChatModel chatModel = buildChatModel();

            // Build research graph
            log.info("📊 Building research state graph...");
            var researchGraph = graphBuilder.createResearchGraph();

            // Compile graph
            log.info("⚙️ Compiling research graph...");
            CompileConfig compileConfig = CompileConfig.builder()
                .checkpointSaver(new MemorySaver())
                .build();
            
            CompiledGraph<ResearchState> compiledGraph = researchGraph.compile(compileConfig);

            // Create initial state
            Map<String, Object> initialState = graphBuilder.createInitialState(
                request.getResearchTopic(),
                requestId,
                request.getUserId(),
                request.getMaxResearchLoops(),
                request.getSearchEngine(),
                request.getMaxSearchResults(),
                request.getFetchFullPage()
            );

            // Create run configuration
            RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(requestId)
                .build();

            log.info("🎯 Starting research graph execution, initial state: {}", initialState.keySet());

            // Execute research graph
            ResearchState finalState = null;
            int nodeCount = 0;
            
            for (var nodeOutput : compiledGraph.stream(initialState, runnableConfig)) {
                nodeCount++;
                finalState = nodeOutput.state();
                
                String currentTopic = finalState.researchTopic().orElse("Unknown");
                int currentLoop = finalState.researchLoopCount();
                boolean isSuccess = finalState.success();
                
                log.info("📋 Node[{}] execution completed - Topic: {}, Loop: {}, Status: {}", 
                    nodeCount, currentTopic, currentLoop, isSuccess ? "Normal" : "Abnormal");
                
                // If error occurs, exit early
                if (!isSuccess) {
                    String errorMsg = finalState.errorMessage().orElse("Unknown error");
                    log.warn("⚠️ Error occurred during research: {}", errorMsg);
                    break;
                }
                
                // Prevent infinite loop
                if (nodeCount > 50) {
                    log.warn("⚠️ Too many node executions, forcing exit");
                    break;
                }
            }

            if (finalState == null) {
                throw new IllegalStateException("Graph execution did not return any state");
            }

            log.info("✅ Research graph execution completed, executed {} nodes", nodeCount);

            return buildSuccessResponse(request, requestId, finalState, startTime);

        } catch (GraphStateException e) {
            log.error("❌ Graph state exception, request ID: " + requestId, e);
            return buildErrorResponse(request, requestId, "Graph state exception: " + e.getMessage(), startTime);
        } catch (Exception e) {
            log.error("❌ Deep research execution failed, request ID: " + requestId, e);
            return buildErrorResponse(request, requestId, "Research execution failed: " + e.getMessage(), startTime);
        }
    }

    /**
     * Build ChatModel
     */
    private ChatModel buildChatModel() {
        ResearchModelConfig researchModelConfig = researchConfig.getModel();

        log.info("🤖 Building ChatModel, model: {}, temperature: {}, max tokens: {}", 
                researchModelConfig.getModelName(), researchModelConfig.getTemperature(), researchModelConfig.getMaxTokens());

        return OpenAiChatModel.builder()
                .modelName(researchModelConfig.getModelName())
                .apiKey(researchModelConfig.getApiKey())
                .baseUrl(researchModelConfig.getApiUrl())
                .temperature(researchModelConfig.getTemperature())
                .maxTokens(researchModelConfig.getMaxTokens())
                .logRequests(researchModelConfig.getLogRequests())
                .logResponses(researchModelConfig.getLogResponses())
                .build();
    }

    /**
     * Build success response
     */
    private ResearchResponse buildSuccessResponse(
            ResearchRequest request, 
            String requestId, 
            ResearchState finalState, 
            LocalDateTime startTime) {

        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        String finalSummary = finalState.runningSummary().orElse("Research failed to generate summary");
        List<String> sourcesGathered = finalState.sourcesGathered();
        Integer actualLoops = finalState.researchLoopCount();
        Boolean success = finalState.success();

        log.info("📈 Research completion statistics - Loops: {}, Sources: {}, Duration: {}ms, Success: {}", 
            actualLoops, sourcesGathered.size(), durationMs, success);

        return ResearchResponse.builder()
                .requestId(requestId)
                .researchTopic(request.getResearchTopic())
                .finalSummary(finalSummary)
                .actualLoops(actualLoops)
                .sourcesGathered(sourcesGathered)
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .success(success)
                .status(success ? ResearchResponse.ResearchStatus.COMPLETED : ResearchResponse.ResearchStatus.FAILED)
                .errorMessage(finalState.errorMessage().orElse(null))
                .build();
    }

    /**
     * Build error response
     */
    private ResearchResponse buildErrorResponse(
            ResearchRequest request, 
            String requestId, 
            String errorMessage, 
            LocalDateTime startTime) {

        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        return ResearchResponse.builder()
                .requestId(requestId)
                .researchTopic(request.getResearchTopic())
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .success(false)
                .errorMessage(errorMessage)
                .status(ResearchResponse.ResearchStatus.FAILED)
                .sourcesGathered(List.of())
                .actualLoops(0)
                .build();
    }
}