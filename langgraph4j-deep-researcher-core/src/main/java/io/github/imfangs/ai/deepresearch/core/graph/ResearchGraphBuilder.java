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
 * 研究图构建器
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
     * 创建研究状态图
     * 
     * 研究流程：
     * 1. 生成搜索查询 (generate_query)
     * 2. 执行Web搜索 (web_search)
     * 3. 总结搜索结果 (summarize)
     * 4. 反思和分析 (reflect)
     * 5. 路由决策 (route) - 继续研究 or 结束
     * 6. 最终化总结 (finalize)
     */
    public StateGraph<ResearchState> createResearchGraph() throws GraphStateException {
        log.info("创建深度研究状态图...");

        var serializer = new LC4jJacksonStateSerializer<ResearchState>(ResearchState::new);
        serializer.objectMapper().registerModule(new JavaTimeModule());
        serializer.objectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

//        StateGraph<ResearchState> workflow = new StateGraph<>(ResearchState.SCHEMA, serializer)
        StateGraph<ResearchState> workflow = new StateGraph<>(ResearchState.SCHEMA, new ResearchStateSerializer())
            // 添加研究节点
            .addNode("generate_query", node_async(queryGeneratorNode))
            .addNode("web_search", node_async(webSearchNode))
            .addNode("summarize", node_async(summarizerNode))
            .addNode("reflect", node_async(reflectionNode))
            .addNode("route", node_async(routerNode))
            .addNode("finalize", node_async(finalizerNode))

            // 设置入口点：开始时生成查询
            .addEdge(START, "generate_query")

            // 线性流程：查询 -> 搜索 -> 总结 -> 反思 -> 路由
            .addEdge("generate_query", "web_search")
            .addEdge("web_search", "summarize")
            .addEdge("summarize", "reflect")
            .addEdge("reflect", "route")

            // 路由条件边：根据路由决策继续或结束
            .addConditionalEdges(
                "route",
                // 路由条件函数：检查是否继续研究
                edge_async(state -> {
                    // 将AgentState转换为ResearchState以访问便利方法
                    ResearchState researchState = new ResearchState(state.data());
                    
                    // 检查是否达到最大循环次数
                    if (researchState.hasReachedMaxLoops()) {
                        log.info("已达到最大研究循环次数 {}, 结束研究", researchState.maxResearchLoops());
                        return "finalize";
                    }

                    // 检查是否有错误
                    if (!researchState.success()) {
                        log.warn("研究过程中出现错误: {}, 结束研究", researchState.errorMessage().orElse("未知错误"));
                        return "finalize";
                    }

                    // 检查是否有足够的信息（基于总结长度的简单判断）
                    String summary = researchState.runningSummary().orElse("");
                    if (summary.length() > 1000 && researchState.researchLoopCount() >= 2) {
                        log.info("已收集到足够信息，循环次数: {}, 总结长度: {}", researchState.researchLoopCount(), summary.length());
                        return "finalize";
                    }

                    // 继续研究
                    log.info("继续研究，当前循环次数: {}/{}", researchState.researchLoopCount(), researchState.maxResearchLoops());
                    return "continue";
                }),

                // 路由映射
                Map.of(
                    "continue", "generate_query",  // 继续研究：回到查询生成
                    "finalize", "finalize"         // 结束研究：进入最终化
                )
            )

            // 最终化后结束
            .addEdge("finalize", END);

        log.info("研究状态图创建完成");
        return workflow;
    }

    /**
     * 创建初始状态
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
