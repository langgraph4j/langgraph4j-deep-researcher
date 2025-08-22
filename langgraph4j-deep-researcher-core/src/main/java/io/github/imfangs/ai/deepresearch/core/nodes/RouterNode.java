package io.github.imfangs.ai.deepresearch.core.nodes;

import io.github.imfangs.ai.deepresearch.api.state.ResearchState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 路由节点
 * 
 * 决定研究流程的下一步：继续研究或结束
 * 
 * @author imfangs
 */
@Slf4j
@Component
public class RouterNode implements NodeAction<ResearchState> {

    @Override
    public Map<String, Object> apply(ResearchState state) {
            try {
                log.info("🛤️ 进行路由决策");

                // 标记节点开始
                Map<String, Object> nodeStart = state.markNodeStart();

                // 路由决策逻辑在ResearchGraphBuilder中的条件边实现
                // 这个节点主要用于记录决策过程和更新状态

                String decision = makeRoutingDecision(state);
                log.info("路由决策: {}", decision);

                // 返回状态更新
                return Map.of(
                    "metadata", Map.of(
                        "routing_decision", decision,
                        "routing_timestamp", System.currentTimeMillis(),
                        "loop_count_at_decision", state.researchLoopCount()
                    ),
                    "current_node_start_time", nodeStart.get("current_node_start_time")
                );

            } catch (Exception e) {
                log.error("路由决策失败", e);
                return state.setError("路由决策失败: " + e.getMessage());
            }
    }

    /**
     * 路由决策逻辑
     */
    private String makeRoutingDecision(ResearchState state) {
        // 检查是否达到最大循环次数
        if (state.hasReachedMaxLoops()) {
            return "达到最大循环次数，结束研究";
        }

        // 检查是否有错误
        if (!state.success()) {
            return "检测到错误，结束研究";
        }

        // 检查反思结果
        Map<String, Object> metadata = state.metadata();
        Boolean needMoreResearch = (Boolean) metadata.get("need_more_research");
        if (needMoreResearch != null && !needMoreResearch) {
            return "反思表明信息充足，结束研究";
        }

        // 检查信息量（基于总结长度和循环次数）
        String summary = state.runningSummary().orElse("");
        int loopCount = state.researchLoopCount();
        
        if (summary.length() > 1500 && loopCount >= 2) {
            return "收集到充足信息，结束研究";
        }

        if (loopCount >= 1 && summary.length() > 2000) {
            return "信息量已足够，结束研究";
        }

        return "继续研究以获取更多信息";
    }
}
