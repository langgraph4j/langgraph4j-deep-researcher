package io.github.imfangs.ai.deepresearch.core.nodes;

import io.github.imfangs.ai.deepresearch.api.state.ResearchState;
import io.github.imfangs.ai.deepresearch.config.PromptTemplates;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 最终化节点
 * 
 * 负责生成最终的研究报告和总结
 * 
 * @author imfangs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FinalizerNode implements NodeAction<ResearchState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(ResearchState state) {
            try {
                log.info("🏁 开始生成最终研究报告");

                // 标记节点开始
                Map<String, Object> nodeStart = state.markNodeStart();

                String researchTopic = state.researchTopic()
                    .orElseThrow(() -> new IllegalStateException("缺少研究主题"));

                String currentSummary = state.runningSummary().orElse("未生成研究总结");

                // 构建提示词
                String systemPrompt = PromptTemplates.FINALIZATION_SYSTEM;
                String userMessage = buildUserMessage(researchTopic, currentSummary, state);

                log.debug("最终化系统提示词: {}", systemPrompt);
                log.debug("最终化用户消息长度: {} 字符", userMessage.length());

                // 调用LLM生成最终报告
                String finalSummary = chatModel.chat(userMessage);

                log.info("最终研究报告生成完成，长度: {} 字符", finalSummary.length());

                // 计算执行统计
                LocalDateTime endTime = LocalDateTime.now();
                long totalDuration = state.getTotalDuration();
                int totalLoops = state.researchLoopCount();
                int totalSources = state.sourcesGathered().size();

                log.info("研究完成统计 - 循环次数: {}, 源数量: {}, 总耗时: {}ms", 
                    totalLoops, totalSources, totalDuration);

                // 返回最终状态更新
                return Map.of(
                    "running_summary", finalSummary,
                    "success", true,
                    "metadata", Map.of(
                        "final_report_generated", true,
                        "completion_timestamp", System.currentTimeMillis(),
                        "total_duration_ms", totalDuration,
                        "total_loops_completed", totalLoops,
                        "total_sources_gathered", totalSources,
                        "final_summary_length", finalSummary.length()
                    ),
                    "current_node_start_time", nodeStart.get("current_node_start_time")
                );

            } catch (Exception e) {
                log.error("最终化处理失败", e);
                return state.setError("最终化处理失败: " + e.getMessage());
            }
    }

    /**
     * 构建用户消息
     */
    private String buildUserMessage(String researchTopic, String currentSummary, ResearchState state) {
        StringBuilder userMessage = new StringBuilder();
        
        // 基本信息
        userMessage.append("研究主题: ").append(researchTopic);
        userMessage.append("\n完成循环数: ").append(state.researchLoopCount());
        userMessage.append("\n收集源数量: ").append(state.sourcesGathered().size());
        
        // 执行统计
        long duration = state.getTotalDuration();
        if (duration > 0) {
            userMessage.append("\n总执行时间: ").append(formatDuration(duration));
        }

        // 当前总结
        userMessage.append("\n\n研究总结:\n").append(currentSummary);

        // 源信息列表
        List<String> sources = state.sourcesGathered();
        if (!sources.isEmpty()) {
            userMessage.append("\n\n参考源:\n");
            for (int i = 0; i < sources.size() && i < 10; i++) { // 最多显示10个源
                userMessage.append(i + 1).append(". ").append(sources.get(i)).append("\n");
            }
            if (sources.size() > 10) {
                userMessage.append("... (共 ").append(sources.size()).append(" 个源)\n");
            }
        }

        userMessage.append("\n请生成一个专业、完整的最终研究报告，包含清晰的结构和结论。");

        return userMessage.toString();
    }

    /**
     * 格式化持续时间
     */
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        if (seconds < 60) {
            return seconds + "秒";
        } else {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "分" + remainingSeconds + "秒";
        }
    }
}
