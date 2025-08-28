package io.github.imfangs.ai.deepresearch.config;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Research Model Configuration
 */
@Data
public class ResearchModelConfig {

    /**
     * App ID
     */
    @NotBlank(message = "API KEY cannot be empty")
    private String apiKey;

    /**
     * API URL
     */
    @NotBlank(message = "API URL cannot be empty")
    private String apiUrl;

    /**
     * Default model name to use
     */
    @NotBlank(message = "Model name cannot be empty")
    private String modelName = "gpt-4o-mini";

    /**
     * Model temperature parameter
     */
    @DecimalMin(value = "0.0", message = "Temperature parameter cannot be less than 0")
    @DecimalMax(value = "2.0", message = "Temperature parameter cannot be greater than 2")
    private Double temperature = 0.1;

    /**
     * Maximum token count
     */
    @Min(value = 100, message = "Maximum token count must be at least 100")
    @Max(value = 16000, message = "Maximum token count cannot exceed 16000")
    private Integer maxTokens = 4096;

    /**
     * Whether to enable request logging
     */
    private Boolean logRequests = true;

    /**
     * Whether to enable response logging
     */
    private Boolean logResponses = true;
}