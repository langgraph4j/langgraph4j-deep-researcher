package io.github.imfangs.ai.deepresearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

/**
 * Deep research core configuration class
 *
 * @author imfangs
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deep-research")
@Validated
public class ResearchConfig {

    /**
     * AI model configuration
     */
    @Valid
    private ResearchModelConfig model = new ResearchModelConfig();

    /**
     * Search engine configuration
     */
    @Valid
    private SearchConfig search = new SearchConfig();

    /**
     * Research flow configuration
     */
    @Valid
    private FlowConfig flow = new FlowConfig();


}
