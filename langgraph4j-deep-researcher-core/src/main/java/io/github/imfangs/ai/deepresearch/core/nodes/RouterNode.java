package io.github.imfangs.ai.deepresearch.core.nodes;

import io.github.imfangs.ai.deepresearch.api.state.ResearchState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Router node
 * 
 * Determines the next step in the research flow: continue research or end
 * 
 * @author imfangs
 */
@Slf4j
@Component
public class RouterNode implements NodeAction<ResearchState> {

    @Override
    public Map<String, Object> apply(ResearchState state) {
            try {
                log.info("🛤️ Making routing decision");

                // Mark node start
                Map<String, Object> nodeStart = state.markNodeStart();

                // Routing decision logic is implemented in conditional edges in ResearchGraphBuilder
                // This node is mainly used to record decision process and update state

                String decision = makeRoutingDecision(state);
                log.info("Routing decision: {}", decision);

                // Return state updates
                return Map.of(
                    "metadata", Map.of(
                        "routing_decision", decision,
                        "routing_timestamp", System.currentTimeMillis(),
                        "loop_count_at_decision", state.researchLoopCount()
                    ),
                    "current_node_start_time", nodeStart.get("current_node_start_time")
                );

            } catch (Exception e) {
                log.error("Routing decision failed", e);
                return state.setError("Routing decision failed: " + e.getMessage());
            }
    }

    /**
     * Routing decision logic
     */
    private String makeRoutingDecision(ResearchState state) {
        // Check if maximum loop count is reached
        if (state.hasReachedMaxLoops()) {
            return "Reached maximum loop count, ending research";
        }

        // Check if there are any errors
        if (!state.success()) {
            return "Error detected, ending research";
        }

        // Check reflection results
        Map<String, Object> metadata = state.metadata();
        Boolean needMoreResearch = (Boolean) metadata.get("need_more_research");
        if (needMoreResearch != null && !needMoreResearch) {
            return "Reflection indicates sufficient information, ending research";
        }

        // Check information volume (based on summary length and loop count)
        String summary = state.runningSummary().orElse("");
        int loopCount = state.researchLoopCount();
        
        if (summary.length() > 1500 && loopCount >= 2) {
            return "Sufficient information collected, ending research";
        }

        if (loopCount >= 1 && summary.length() > 2000) {
            return "Information volume is sufficient, ending research";
        }

        return "Continue research to obtain more information";
    }
}
