package io.github.imfangs.ai.deepresearch.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Search configuration
 */
@Data
public class SearchConfig {

    /**
     * Default search engine
     */
    @NotBlank(message = "Default search engine cannot be empty")
    private String defaultEngine = "tavily";

    /**
     * Tavily API configuration
     */
    @Valid
    private TavilyConfig tavily = new TavilyConfig();

    // Getter method
    public TavilyConfig getTavily() {
        return tavily;
    }

}
