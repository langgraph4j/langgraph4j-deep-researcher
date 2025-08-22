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

import java.util.List;
import java.util.Map;

/**
 * 总结节点
 * 
 * 负责总结搜索结果并更新运行中的总结
 * 
 * @author imfangs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SummarizerNode implements NodeAction<ResearchState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(ResearchState state) {
        try {
            log.info("📝 开始总结搜索结果");

            // 标记节点开始
            Map<String, Object> nodeStart = state.markNodeStart();

            List<String> searchResults = state.webSearchResults();
            if (searchResults.isEmpty()) {
                log.warn("没有搜索结果可供总结");
                return Map.of(
                    "current_node_start_time", nodeStart.get("current_node_start_time")
                );
            }

            String researchTopic = state.researchTopic()
                .orElseThrow(() -> new IllegalStateException("缺少研究主题"));

            // 构建提示词
            String systemPrompt = PromptTemplates.SUMMARIZATION_SYSTEM;
            String userMessage = buildUserMessage(state, researchTopic, searchResults);

            log.debug("总结系统提示词: {}", systemPrompt);
            log.debug("总结用户消息长度: {} 字符", userMessage.length());

            // 调用LLM生成总结
            String newSummary = chatModel.chat(userMessage);

            // 增加循环计数
            Integer newLoopCount = state.researchLoopCount() + 1;

            log.info("总结完成，循环次数更新为: {}, 总结长度: {} 字符", 
                newLoopCount, newSummary.length());

            // 将源信息添加到收集列表
            List<String> newSources = extractSources(searchResults);

            // 返回状态更新
            return Map.of(
                "running_summary", newSummary,
                "research_loop_count", newLoopCount,
                "sources_gathered", newSources,
                "current_node_start_time", nodeStart.get("current_node_start_time")
            );

        } catch (Exception e) {
            log.error("总结生成失败", e);
            return state.setError("总结生成失败: " + e.getMessage());
        }
    }

    /**
     * 构建用户消息
     */
    private String buildUserMessage(ResearchState state, String researchTopic, List<String> searchResults) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append("研究主题: ").append(researchTopic);

        // 如果有之前的总结，包含它
        String previousSummary = state.runningSummary().orElse("");
        if (!previousSummary.isEmpty()) {
            userMessage.append("\n\n之前的研究总结:\n").append(previousSummary);
        }

        // 添加新的搜索结果
        userMessage.append("\n\n最新搜索结果:\n");
        for (int i = 0; i < searchResults.size(); i++) {
            userMessage.append(i + 1).append(". ").append(searchResults.get(i)).append("\n");
        }

        userMessage.append("\n请结合之前的总结和新的搜索结果，生成一个更全面、更准确的研究总结。");

        return userMessage.toString();
    }

    /**
     * 从搜索结果中提取源信息
     */
    private List<String> extractSources(List<String> searchResults) {
        return searchResults.stream()
            .map(result -> {
                // 提取URL部分作为源
                if (result.contains(" - ")) {
                    String[] parts = result.split(" - ", 2);
                    if (parts.length > 0 && parts[0].contains("] ")) {
                        String[] titleUrl = parts[0].split("] ", 2);
                        if (titleUrl.length > 1) {
                            return titleUrl[1]; // 返回URL部分
                        }
                    }
                }
                return result; // 如果无法解析，返回原始结果
            })
            .toList();
    }
}
