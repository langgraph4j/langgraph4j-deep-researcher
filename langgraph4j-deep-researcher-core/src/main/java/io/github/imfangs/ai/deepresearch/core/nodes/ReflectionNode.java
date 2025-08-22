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
 * 反思节点
 * 
 * 负责分析当前总结，识别知识缺口和改进方向
 * 
 * @author imfangs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ReflectionNode implements NodeAction<ResearchState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(ResearchState state) {
        try {
            log.info("🤔 开始反思分析");

            // 标记节点开始
            Map<String, Object> nodeStart = state.markNodeStart();

            String researchTopic = state.researchTopic()
                .orElseThrow(() -> new IllegalStateException("缺少研究主题"));

            String currentSummary = state.runningSummary().orElse("");
            if (currentSummary.isEmpty()) {
                log.warn("没有总结可供反思");
                return Map.of(
                    "current_node_start_time", nodeStart.get("current_node_start_time")
                );
            }

            // 构建提示词
            String systemPrompt = PromptTemplates.REFLECTION_SYSTEM;
            String userMessage = buildUserMessage(researchTopic, currentSummary, state);

            log.debug("反思系统提示词: {}", systemPrompt);
            log.debug("反思用户消息长度: {} 字符", userMessage.length());

            // 调用LLM进行反思分析
            String reflectionResult = chatModel.chat(userMessage);

            log.info("反思分析完成，结果长度: {} 字符", reflectionResult.length());
            log.debug("反思结果: {}", reflectionResult);

            // 分析反思结果，决定是否需要继续研究
            boolean needMoreResearch = analyzeReflectionResult(reflectionResult);
            
            log.info("反思结论: {}", needMoreResearch ? "需要更多研究" : "信息已较为完整");

            // 返回状态更新（反思结果可以存储在metadata中）
            return Map.of(
                "metadata", Map.of(
                    "last_reflection", reflectionResult,
                    "need_more_research", needMoreResearch,
                    "reflection_timestamp", System.currentTimeMillis()
                ),
                "current_node_start_time", nodeStart.get("current_node_start_time")
            );

        } catch (Exception e) {
            log.error("反思分析失败", e);
            return state.setError("反思分析失败: " + e.getMessage());
        }
    }

    /**
     * 构建用户消息
     */
    private String buildUserMessage(String researchTopic, String currentSummary, ResearchState state) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append("研究主题: ").append(researchTopic);
        userMessage.append("\n\n当前循环次数: ").append(state.researchLoopCount());
        userMessage.append("/").append(state.maxResearchLoops());
        
        userMessage.append("\n\n当前研究总结:\n").append(currentSummary);

        // 添加已收集的源信息数量
        int sourcesCount = state.sourcesGathered().size();
        userMessage.append("\n\n已收集源信息数量: ").append(sourcesCount);

        userMessage.append("\n\n请分析当前总结的完整性和准确性，识别可能的知识缺口或需要补充的信息。");

        return userMessage.toString();
    }

    /**
     * 分析反思结果，判断是否需要更多研究
     */
    private boolean analyzeReflectionResult(String reflectionResult) {
        if (reflectionResult == null || reflectionResult.trim().isEmpty()) {
            return false;
        }

        String lowerResult = reflectionResult.toLowerCase();

        // 寻找表示需要更多信息的关键词
        String[] needMoreKeywords = {
            "需要更多", "缺少", "不足", "不完整", "需要补充", "需要进一步",
            "更深入", "更详细", "gap", "missing", "incomplete", "need more",
            "further research", "additional information"
        };

        for (String keyword : needMoreKeywords) {
            if (lowerResult.contains(keyword)) {
                return true;
            }
        }

        // 寻找表示信息充足的关键词
        String[] sufficientKeywords = {
            "充足", "完整", "全面", "足够", "完善", "sufficient", "complete",
            "comprehensive", "adequate", "thorough"
        };

        for (String keyword : sufficientKeywords) {
            if (lowerResult.contains(keyword)) {
                return false;
            }
        }

        // 默认情况下，倾向于认为需要更多研究（除非明确表示充足）
        return true;
    }
}
