package io.github.imfangs.ai.deepresearch.tools.search;

import io.github.imfangs.ai.deepresearch.api.dto.SearchResult;
import io.github.imfangs.ai.deepresearch.config.FlowConfig;
import io.github.imfangs.ai.deepresearch.config.ResearchConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Search engine manager
 * Responsible for managing multiple search engines and providing a unified search interface
 * 
 * @author imfangs
 */
@Slf4j
@Component
public class SearchEngineManager {

    private final ResearchConfig researchConfig;
    private final Map<String, SearchEngine> searchEngines;

    @Autowired
    public SearchEngineManager(ResearchConfig researchConfig, List<SearchEngine> searchEngineList) {
        this.researchConfig = researchConfig;
        this.searchEngines = searchEngineList.stream()
                .collect(Collectors.toMap(
                    SearchEngine::getEngineName, 
                    engine -> engine,
                    (existing, replacement) -> existing  // If duplicate names exist, keep the existing one
                ));
        
        log.info("Search engine manager initialization completed, available engines: {}", 
                getAvailableEngines().stream()
                        .map(SearchEngine::getEngineName)
                        .collect(Collectors.joining(", ")));
    }

    /**
     * Execute search using specified search engine
     *
     * @param engineName Search engine name
     * @param query Search query
     * @param maxResults Maximum number of results
     * @param fetchFullPage Whether to fetch full page content
     * @return List of search results
     */
    public List<SearchResult> search(String engineName, String query, int maxResults, boolean fetchFullPage) {
        SearchEngine engine = getSearchEngine(engineName);
        if (engine == null) {
            log.error("Search engine not found: {}", engineName);
            return Collections.emptyList();
        }

        if (!engine.isAvailable()) {
            log.warn("Search engine {} is currently unavailable", engineName);
            return Collections.emptyList();
        }

        return engine.search(query, maxResults, fetchFullPage);
    }

    /**
     * Execute search using default search engine
     *
     * @param query Search query
     * @param maxResults Maximum number of results
     * @param fetchFullPage Whether to fetch full page content
     * @return List of search results
     */
    public List<SearchResult> searchWithDefault(String query, int maxResults, boolean fetchFullPage) {
        String defaultEngine = researchConfig.getSearch().getDefaultEngine();
        return search(defaultEngine, query, maxResults, fetchFullPage);
    }

    /**
     * Execute search using default search engine (using default parameters from configuration)
     *
     * @param query Search query
     * @return List of search results
     */
    public List<SearchResult> searchWithDefault(String query) {
        FlowConfig flowConfig = researchConfig.getFlow();
        return searchWithDefault(
                query, 
                flowConfig.getDefaultMaxSearchResults(),
                flowConfig.getDefaultFetchFullPage()
        );
    }

    /**
     * Get search engine by name
     *
     * @param engineName Search engine name
     * @return Search engine instance, returns null if not found
     */
    public SearchEngine getSearchEngine(String engineName) {
        return searchEngines.get(engineName);
    }

    /**
     * Get all available search engines
     *
     * @return List of available search engines
     */
    public List<SearchEngine> getAvailableEngines() {
        return searchEngines.values().stream()
                .filter(SearchEngine::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Get all search engine names
     *
     * @return List of search engine names
     */
    public Set<String> getAllEngineNames() {
        return searchEngines.keySet();
    }

    /**
     * Get all available search engine names
     *
     * @return List of available search engine names
     */
    public List<String> getAvailableEngineNames() {
        return getAvailableEngines().stream()
                .map(SearchEngine::getEngineName)
                .collect(Collectors.toList());
    }

    /**
     * Check if specified search engine is available
     *
     * @param engineName Search engine name
     * @return Whether available
     */
    public boolean isEngineAvailable(String engineName) {
        SearchEngine engine = getSearchEngine(engineName);
        return engine != null && engine.isAvailable();
    }

    /**
     * Deduplicate and format search results
     *
     * @param searchResults List of search results
     * @param maxTokensPerSource Maximum tokens per source
     * @return Formatted search results string
     */
    public String formatSearchResults(List<SearchResult> searchResults, int maxTokensPerSource) {
        if (searchResults == null || searchResults.isEmpty()) {
            return "No relevant search results found";
        }

        // Deduplicate by URL
        Map<String, SearchResult> uniqueResults = new LinkedHashMap<>();
        for (SearchResult result : searchResults) {
            if (!uniqueResults.containsKey(result.getUrl())) {
                uniqueResults.put(result.getUrl(), result);
            }
        }

        StringBuilder formatted = new StringBuilder();
        formatted.append("Sources:\n\n");

        int index = 1;
        int charsPerToken = researchConfig.getFlow().getCharsPerToken();
        int maxChars = maxTokensPerSource * charsPerToken;

        for (SearchResult result : uniqueResults.values()) {
            formatted.append(String.format("Source: %s\n===\n", result.getTitle()));
            formatted.append(String.format("URL: %s\n===\n", result.getUrl()));
            formatted.append(String.format("Most relevant content from source: %s\n===\n", result.getContent()));

            // If raw content exists and exceeds limit, truncate it
            if (result.getRawContent() != null && !result.getRawContent().isEmpty()) {
                String rawContent = result.getRawContent();
                if (rawContent.length() > maxChars) {
                    rawContent = rawContent.substring(0, maxChars) + "... [truncated]";
                }
                formatted.append(String.format("Full source content limited to %d tokens: %s\n\n", 
                        maxTokensPerSource, rawContent));
            }

            index++;
        }

        return formatted.toString().trim();
    }
}
