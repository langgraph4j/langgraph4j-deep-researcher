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

import java.util.Map;

/**
 * 查询生成节点
 * 
 * 负责生成用于Web搜索的查询语句
 * 
 * @author imfangs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class QueryGeneratorNode implements NodeAction<ResearchState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(ResearchState state) {
        try {
            log.info("🔍 开始生成搜索查询，循环次数: {}", state.researchLoopCount());

            // 标记节点开始
            Map<String, Object> nodeStart = state.markNodeStart();

            String researchTopic = state.researchTopic()
                .orElseThrow(() -> new IllegalStateException("缺少研究主题"));

            // 构建提示词
            String systemPrompt = PromptTemplates.QUERY_GENERATION_SYSTEM;
            String userMessage = buildUserMessage(state, researchTopic);

            log.debug("系统提示词: {}", systemPrompt);
            log.debug("用户消息: {}", userMessage);

            // 调用LLM生成查询
            String response = chatModel.chat(userMessage);

            // 清理响应，提取实际的搜索查询
            String searchQuery = cleanResponse(response);
            log.info("生成的搜索查询: {}", searchQuery);

            // 返回状态更新
            return Map.of(
                "search_query", searchQuery,
                "current_node_start_time", nodeStart.get("current_node_start_time")
            );

        } catch (Exception e) {
            log.error("查询生成失败", e);
            return state.setError("查询生成失败: " + e.getMessage());
        }
    }

    /**
     * 构建用户消息
     */
    private String buildUserMessage(ResearchState state, String researchTopic) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append("研究主题: ").append(researchTopic);

        // 如果是后续循环，包含之前的总结
        if (state.researchLoopCount() > 0) {
            String previousSummary = state.runningSummary().orElse("");
            if (!previousSummary.isEmpty()) {
                userMessage.append("\n\n当前研究进展:\n").append(previousSummary);
            }

            userMessage.append("\n\n请基于已有信息，生成一个新的搜索查询来深入研究或补充缺失的信息。");
        } else {
            userMessage.append("\n\n这是第一次搜索，请生成一个全面的搜索查询来开始研究。");
        }

        return userMessage.toString();
    }

    /**
     * 清理LLM响应，提取搜索查询
     */
    private String cleanResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalStateException("LLM 返回空响应");
        }

        // 移除可能的格式化标记
        String cleaned = response.trim()
            .replaceFirst("^搜索查询[:：]?\\s*", "")
            .replaceFirst("^查询[:：]?\\s*", "")
            .replaceFirst("^Query[:：]?\\s*", "")
            .replaceAll("^[\"']|[\"']$", ""); // 移除引号

        // 取第一行作为查询（防止LLM返回多行）
        String[] lines = cleaned.split("\n");
        String query = lines[0].trim();

        if (query.isEmpty()) {
            throw new IllegalStateException("无法从 LLM 响应中提取有效查询");
        }

        return query;
    }
}
