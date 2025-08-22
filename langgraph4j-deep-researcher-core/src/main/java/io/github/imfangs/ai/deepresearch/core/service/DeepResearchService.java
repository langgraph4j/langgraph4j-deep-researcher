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
 * 深度研究服务
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
     * 执行深度研究
     * 
     * @param request 研究请求
     * @return 研究响应
     */
    public ResearchResponse executeResearch(ResearchRequest request) {
        // 生成请求ID（如果未提供）
        String requestId = request.getRequestId() != null ? 
                request.getRequestId() : UUID.randomUUID().toString();

        log.info("🚀 开始执行深度研究，请求ID: {}, 研究主题: {}", requestId, request.getResearchTopic());

        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 构建 ChatModel
            ChatModel chatModel = buildChatModel();

            // 构建研究图
            log.info("📊 构建研究状态图...");
            var researchGraph = graphBuilder.createResearchGraph();

            // 编译图
            log.info("⚙️ 编译研究图...");
            CompileConfig compileConfig = CompileConfig.builder()
                .checkpointSaver(new MemorySaver())
                .build();
            
            CompiledGraph<ResearchState> compiledGraph = researchGraph.compile(compileConfig);

            // 创建初始状态
            Map<String, Object> initialState = graphBuilder.createInitialState(
                request.getResearchTopic(),
                requestId,
                request.getUserId(),
                request.getMaxResearchLoops(),
                request.getSearchEngine(),
                request.getMaxSearchResults(),
                request.getFetchFullPage()
            );

            // 创建运行配置
            RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(requestId)
                .build();

            log.info("🎯 开始执行研究图，初始状态: {}", initialState.keySet());

            // 执行研究图
            ResearchState finalState = null;
            int nodeCount = 0;
            
            for (var nodeOutput : compiledGraph.stream(initialState, runnableConfig)) {
                nodeCount++;
                finalState = nodeOutput.state();
                
                String currentTopic = finalState.researchTopic().orElse("未知");
                int currentLoop = finalState.researchLoopCount();
                boolean isSuccess = finalState.success();
                
                log.info("📋 节点[{}]执行完成 - 主题: {}, 循环: {}, 状态: {}", 
                    nodeCount, currentTopic, currentLoop, isSuccess ? "正常" : "异常");
                
                // 如果出现错误，提前退出
                if (!isSuccess) {
                    String errorMsg = finalState.errorMessage().orElse("未知错误");
                    log.warn("⚠️ 研究过程中出现错误: {}", errorMsg);
                    break;
                }
                
                // 防止无限循环
                if (nodeCount > 50) {
                    log.warn("⚠️ 节点执行次数过多，强制退出");
                    break;
                }
            }

            if (finalState == null) {
                throw new IllegalStateException("图执行未返回任何状态");
            }

            log.info("✅ 研究图执行完成，共执行 {} 个节点", nodeCount);

            return buildSuccessResponse(request, requestId, finalState, startTime);

        } catch (GraphStateException e) {
            log.error("❌ 图状态异常，请求ID: " + requestId, e);
            return buildErrorResponse(request, requestId, "图状态异常: " + e.getMessage(), startTime);
        } catch (Exception e) {
            log.error("❌ 深度研究执行失败，请求ID: " + requestId, e);
            return buildErrorResponse(request, requestId, "研究执行失败: " + e.getMessage(), startTime);
        }
    }

    /**
     * 构建 ChatModel
     */
    private ChatModel buildChatModel() {
        ResearchModelConfig researchModelConfig = researchConfig.getModel();

        log.info("🤖 构建 ChatModel，模型: {}, 温度: {}, 最大Token: {}", 
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
     * 构建成功响应
     */
    private ResearchResponse buildSuccessResponse(
            ResearchRequest request, 
            String requestId, 
            ResearchState finalState, 
            LocalDateTime startTime) {

        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        String finalSummary = finalState.runningSummary().orElse("研究未能生成总结");
        List<String> sourcesGathered = finalState.sourcesGathered();
        Integer actualLoops = finalState.researchLoopCount();
        Boolean success = finalState.success();

        log.info("📈 研究完成统计 - 循环: {}, 源: {}, 耗时: {}ms, 成功: {}", 
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
     * 构建错误响应
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