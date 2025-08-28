package io.github.imfangs.ai.deepresearch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Search result data transfer object
 * 
 * @author imfangs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    /**
     * Title
     */
    @JsonProperty("title")
    private String title;

    /**
     * URL
     */
    @JsonProperty("url")
    private String url;

    /**
     * Summary content
     */
    @JsonProperty("content")
    private String content;

    /**
     * Raw page content (if full page fetching is enabled)
     */
    @JsonProperty("raw_content")
    private String rawContent;

    /**
     * Relevance score
     */
    @JsonProperty("score")
    private Double score;

    /**
     * Additional metadata returned by search engine
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * Source search engine
     */
    @JsonProperty("source_engine")
    private String sourceEngine;
}
