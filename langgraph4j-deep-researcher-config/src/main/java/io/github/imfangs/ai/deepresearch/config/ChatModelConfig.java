package io.github.imfangs.ai.deepresearch.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 相关 Bean 配置
 */
@Configuration
@RequiredArgsConstructor
public class ChatModelConfig {

    private final ResearchConfig researchConfig;

    /**
     * 提供 ChatModel Bean，供各节点注入使用
     */
    @Bean
    public ChatModel chatModel() {
        ResearchModelConfig cfg = researchConfig.getModel();
        return OpenAiChatModel.builder()
                .modelName(cfg.getModelName())
                .apiKey(cfg.getApiKey())
                .baseUrl(cfg.getApiUrl())
                .temperature(cfg.getTemperature())
                .maxTokens(cfg.getMaxTokens())
                .logRequests(Boolean.TRUE.equals(cfg.getLogRequests()))
                .logResponses(Boolean.TRUE.equals(cfg.getLogResponses()))
                .build();
    }
}


