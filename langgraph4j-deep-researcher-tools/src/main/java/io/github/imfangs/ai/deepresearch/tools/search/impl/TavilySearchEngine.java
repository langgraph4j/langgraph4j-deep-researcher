package io.github.imfangs.ai.deepresearch.tools.search.impl;

import io.github.imfangs.ai.deepresearch.api.dto.SearchResult;
import io.github.imfangs.ai.deepresearch.config.ResearchConfig;
import io.github.imfangs.ai.deepresearch.config.SearchConfig;
import io.github.imfangs.ai.deepresearch.config.TavilyConfig;
import io.github.imfangs.ai.deepresearch.tools.search.SearchEngine;
import dev.langchain4j.web.search.WebSearchOrganicResult;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tavily search engine implementation
 * 
 * @author imfangs
 */
@Slf4j
@Component("tavilySearchEngine")
public class TavilySearchEngine implements SearchEngine {

    private final ResearchConfig researchConfig;
    private TavilyWebSearchEngine tavilyWebSearchEngine;

    @Autowired
    public TavilySearchEngine(ResearchConfig researchConfig) {
        this.researchConfig = researchConfig;
        initializeSearchEngine();
    }

    /**
     * Initialize search engine
     */
    private void initializeSearchEngine() {
        try {
            TavilyConfig tavilyConfig =
                researchConfig.getSearch().getTavily();
            
            if (!StringUtils.hasText(tavilyConfig.getApiKey())) {
                log.warn("Tavily API Key not configured, Tavily search engine will be unavailable");
                return;
            }

            this.tavilyWebSearchEngine = TavilyWebSearchEngine.builder()
                    .apiKey(tavilyConfig.getApiKey())
                    .baseUrl(tavilyConfig.getBaseUrl())
                    .timeout(Duration.ofSeconds(tavilyConfig.getTimeoutSeconds()))
                    .searchDepth(tavilyConfig.getSearchDepth())
                    .includeAnswer(tavilyConfig.getIncludeAnswer())
                    .includeRawContent(tavilyConfig.getIncludeRawContent())
                    .build();

            log.info("Tavily search engine initialized successfully");
        } catch (Exception e) {
            log.error("Tavily search engine initialization failed", e);
        }
    }

    @Override
    public List<SearchResult> search(String query, int maxResults, boolean fetchFullPage) {
        if (!isAvailable()) {
            log.warn("Tavily search engine unavailable, returning empty results");
            return Collections.emptyList();
        }

        try {
            log.info("Using Tavily search: query={}, maxResults={}, fetchFullPage={}", 
                    query, maxResults, fetchFullPage);

            WebSearchRequest request = WebSearchRequest.builder()
                    .searchTerms(query)
                    .maxResults(maxResults)
                    .build();

            WebSearchResults results = tavilyWebSearchEngine.search(request);
            
            List<SearchResult> searchResults = results.results().stream()
                    .map(this::convertToSearchResult)
                    .collect(Collectors.toList());

            log.info("Tavily search completed, obtained {} results", searchResults.size());
            return searchResults;

        } catch (Exception e) {
            log.error("Tavily search failed: query=" + query, e);
            return Collections.emptyList();
        }
    }

    /**
     * Convert LangChain4j search results to internal format
     */
    private SearchResult convertToSearchResult(WebSearchOrganicResult result) {
        Map<String, Object> metadata = new HashMap<>();
        
        // Add additional metadata
        if (result.metadata() != null) {
            metadata.putAll(result.metadata());
        }

        return SearchResult.builder()
                .title(result.title())
                .url(result.url() != null ? result.url().toString() : "")
                .content(result.snippet())
                .rawContent(result.content())
                .score(extractScore(result.metadata()))
                .metadata(metadata)
                .sourceEngine(getEngineName())
                .build();
    }

    /**
     * Extract relevance score from metadata
     */
    private Double extractScore(Map<String, String> metadata) {
        if (metadata == null) {
            return null;
        }
        
        String scoreStr = metadata.get("score");
        if (StringUtils.hasText(scoreStr)) {
            try {
                return Double.parseDouble(scoreStr);
            } catch (NumberFormatException e) {
                log.debug("Unable to parse score: {}", scoreStr);
            }
        }
        
        return null;
    }

    @Override
    public String getEngineName() {
        return "tavily";
    }

    @Override
    public boolean isAvailable() {
        return tavilyWebSearchEngine != null && 
               StringUtils.hasText(researchConfig.getSearch().getTavily().getApiKey());
    }
}
