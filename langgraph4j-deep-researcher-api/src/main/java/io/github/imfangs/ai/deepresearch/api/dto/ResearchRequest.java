package io.github.imfangs.ai.deepresearch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Deep research request
 * 
 * @author imfangs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchRequest {

    /**
     * Research topic - The topic the user wants to explore in depth
     */
    @NotBlank(message = "Research topic cannot be empty")
    @Size(max = 500, message = "Research topic cannot exceed 500 characters")
    @JsonProperty("research_topic")
    private String researchTopic;

    /**
     * Maximum research loop count - Controls the number of iterations for deep research
     */
    @Min(value = 1, message = "Research loop count must be at least 1")
    @Max(value = 10, message = "Research loop count cannot exceed 10")
    @JsonProperty("max_research_loops")
    @Builder.Default
    private Integer maxResearchLoops = 3;

    /**
     * Search engine type - Specifies the search tool to use
     */
    @JsonProperty("search_engine")
    @Builder.Default
    private String searchEngine = "tavily";

    /**
     * Whether to fetch full page content
     */
    @JsonProperty("fetch_full_page")
    @Builder.Default
    private Boolean fetchFullPage = true;

    /**
     * Maximum number of search results per search
     */
    @Min(value = 1, message = "Search result count must be at least 1")
    @Max(value = 10, message = "Search result count cannot exceed 10")
    @JsonProperty("max_search_results")
    @Builder.Default
    private Integer maxSearchResults = 3;

    /**
     * Request ID - Used for tracking and logging
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * User ID - Used for session management and personalization
     */
    @JsonProperty("user_id")
    private String userId;
}
