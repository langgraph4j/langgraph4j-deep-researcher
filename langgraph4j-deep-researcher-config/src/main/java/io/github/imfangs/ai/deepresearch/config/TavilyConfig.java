package io.github.imfangs.ai.deepresearch.config;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Tavily configuration
 */
@Data
public class TavilyConfig {
    /**
     * Tavily API Key
     */
    private String apiKey;

    /**
     * API base URL
     */
    private String baseUrl = "https://api.tavily.com/";

    /**
     * Search depth
     */
    private String searchDepth = "advanced";

    /**
     * Whether to include answer
     */
    private Boolean includeAnswer = false;

    /**
     * Whether to include raw content
     */
    private Boolean includeRawContent = true;

    /**
     * Request timeout (seconds)
     */
    @Min(value = 5, message = "Timeout must be at least 5 seconds")
    @Max(value = 300, message = "Timeout cannot exceed 300 seconds")
    private Integer timeoutSeconds = 30;
}