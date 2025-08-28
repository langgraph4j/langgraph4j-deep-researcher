package io.github.imfangs.ai.deepresearch.core.graph;

import io.github.imfangs.ai.deepresearch.api.state.ResearchState;
import io.github.imfangs.ai.deepresearch.api.state.ResearchStateSerializer;
import org.bsc.langgraph4j.langchain4j.serializer.jackson.LC4jJacksonStateSerializer;
import io.github.imfangs.ai.deepresearch.core.nodes.QueryGeneratorNode;
import io.github.imfangs.ai.deepresearch.core.nodes.WebSearchNode;
import io.github.imfangs.ai.deepresearch.core.nodes.SummarizerNode;
import io.github.imfangs.ai.deepresearch.core.nodes.ReflectionNode;
import io.github.imfangs.ai.deepresearch.core.nodes.FinalizerNode;
import io.github.imfangs.ai.deepresearch.core.nodes.RouterNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Research graph builder
 * 
 * @author imfangs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ResearchGraphBuilder {

    private final QueryGeneratorNode queryGeneratorNode;
    private final WebSearchNode webSearchNode;
    private final SummarizerNode summarizerNode;
    private final ReflectionNode reflectionNode;
    private final FinalizerNode finalizerNode;
    private final RouterNode routerNode;

    /**
     * Create research state graph
     * 
     * Research flow:
     * 1. Generate search query (generate_query)
     * 2. Execute web search (web_search)
     * 3. Summarize search results (summarize)
     * 4. Reflect and analyze (reflect)
     * 5. Routing decision (route) - continue research or end
     * 6. Finalize summary (finalize)
     */
    public StateGraph<ResearchState> createResearchGraph() throws GraphStateException {
        log.info("Creating deep research state graph...");

        var serializer = new LC4jJacksonStateSerializer<ResearchState>(ResearchState::new);
        serializer.objectMapper().registerModule(new JavaTimeModule());
        serializer.objectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

//        StateGraph<ResearchState> workflow = new StateGraph<>(ResearchState.SCHEMA, serializer)
        StateGraph<ResearchState> workflow = new StateGraph<>(ResearchState.SCHEMA, new ResearchStateSerializer())
            // Add research nodes
            .addNode("generate_query", node_async(queryGeneratorNode))
            .addNode("web_search", node_async(webSearchNode))
            .addNode("summarize", node_async(summarizerNode))
            .addNode("reflect", node_async(reflectionNode))
            .addNode("route", node_async(routerNode))
            .addNode("finalize", node_async(finalizerNode))

            // Set entry point: start with query generation
            .addEdge(START, "generate_query")

            // Linear flow: query -> search -> summarize -> reflect -> route
            .addEdge("generate_query", "web_search")
            .addEdge("web_search", "summarize")
            .addEdge("summarize", "reflect")
            .addEdge("reflect", "route")

            // Conditional routing edges: continue or end based on routing decision
            .addConditionalEdges(
                "route",
                // Routing condition function: check if research should continue
                edge_async(state -> {
                    // Convert AgentState to ResearchState to access convenience methods
                    ResearchState researchState = new ResearchState(state.data());
                    
                    // Check if maximum loop count is reached
                    if (researchState.hasReachedMaxLoops()) {
                        log.info("Reached maximum research loop count {}, ending research", researchState.maxResearchLoops());
                        return "finalize";
                    }

                    // Check if there are any errors
                    if (!researchState.success()) {
                        log.warn("Error occurred during research: {}, ending research", researchState.errorMessage().orElse("Unknown error"));
                        return "finalize";
                    }

                    // Check if sufficient information is available (simple judgment based on summary length)
                    String summary = researchState.runningSummary().orElse("");
                    if (summary.length() > 1000 && researchState.researchLoopCount() >= 2) {
                        log.info("Sufficient information collected, loop count: {}, summary length: {}", researchState.researchLoopCount(), summary.length());
                        return "finalize";
                    }

                    // Continue research
                                            log.info("Continuing research, current loop count: {}/{}", researchState.researchLoopCount(), researchState.maxResearchLoops());
                    return "continue";
                }),

                // Route mapping
                Map.of(
                    "continue", "generate_query",  // Continue research: back to query generation
                    "finalize", "finalize"         // End research: enter finalization
                )
            )

            // End after finalization
            .addEdge("finalize", END);

        log.info("Research state graph creation completed");
        return workflow;
    }

    /**
     * Create initial state
     */
    public Map<String, Object> createInitialState(
            String researchTopic,
            String requestId,
            String userId,
            Integer maxResearchLoops,
            String searchEngine,
            Integer maxSearchResults,
            Boolean fetchFullPage) {

        return ResearchState.createInitialState(
            researchTopic,
            requestId,
            userId,
            maxResearchLoops,
            searchEngine,
            maxSearchResults,
            fetchFullPage
        );
    }
}
