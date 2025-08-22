package io.github.imfangs.ai.deepresearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

/**
 * 深度研究核心配置类
 *
 * @author imfangs
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deep-research")
@Validated
public class ResearchConfig {

    /**
     * AI 模型配置
     */
    @Valid
    private ResearchModelConfig model = new ResearchModelConfig();

    /**
     * 搜索引擎配置
     */
    @Valid
    private SearchConfig search = new SearchConfig();

    /**
     * 研究流程配置
     */
    @Valid
    private FlowConfig flow = new FlowConfig();


}
