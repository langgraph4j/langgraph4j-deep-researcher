package io.github.imfangs.ai.deepresearch.tools.search;

import io.github.imfangs.ai.deepresearch.api.dto.SearchResult;

import java.util.List;

/**
 * Search engine interface
 * 
 * @author imfangs
 */
public interface SearchEngine {

    /**
     * Execute search
     *
     * @param query Search query
     * @param maxResults Maximum number of results
     * @param fetchFullPage Whether to fetch full page content
     * @return List of search results
     */
    List<SearchResult> search(String query, int maxResults, boolean fetchFullPage);

    /**
     * Get search engine name
     *
     * @return Search engine name
     */
    String getEngineName();

    /**
     * Check if search engine is available
     *
     * @return Whether available
     */
    boolean isAvailable();
}
