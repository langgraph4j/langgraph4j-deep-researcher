package io.github.imfangs.ai.deepresearch.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Research flow configuration
 */
@Data
public class FlowConfig {

    /**
     * Default maximum research loop count
     */
    @Min(value = 1, message = "Research loop count must be at least 1")
    @Max(value = 10, message = "Research loop count cannot exceed 10")
    private Integer defaultMaxLoops = 3;

    /**
     * Default maximum search results per search
     */
    @Min(value = 1, message = "Search result count must be at least 1")
    @Max(value = 10, message = "Search result count cannot exceed 10")
    private Integer defaultMaxSearchResults = 3;

    /**
     * Whether to fetch full page content by default
     */
    private Boolean defaultFetchFullPage = true;

    /**
     * Maximum tokens per source
     */
    @Min(value = 100, message = "Maximum tokens per source must be at least 100")
    @Max(value = 2000, message = "Maximum tokens per source cannot exceed 2000")
    private Integer maxTokensPerSource = 1000;

    /**
     * Character to token conversion ratio
     */
    @Min(value = 2, message = "Character to token conversion ratio must be at least 2")
    @Max(value = 10, message = "Character to token conversion ratio cannot exceed 10")
    private Integer charsPerToken = 4;
}