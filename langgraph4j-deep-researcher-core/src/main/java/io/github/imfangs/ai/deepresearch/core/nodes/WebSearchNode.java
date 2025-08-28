package io.github.imfangs.ai.deepresearch.core.nodes;

import io.github.imfangs.ai.deepresearch.api.dto.SearchResult;
import io.github.imfangs.ai.deepresearch.api.state.ResearchState;
import io.github.imfangs.ai.deepresearch.tools.search.SearchEngineManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Web search node
 * 
 * Responsible for executing web searches and collecting results
 * 
 * @author imfangs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class WebSearchNode implements NodeAction<ResearchState> {

    private final SearchEngineManager searchEngineManager;

    @Override
    public Map<String, Object> apply(ResearchState state) {
            try {
                log.info("🌐 Starting web search");

                // Mark node start
                Map<String, Object> nodeStart = state.markNodeStart();

                String searchQuery = state.searchQuery()
                    .orElseThrow(() -> new IllegalStateException("Missing search query"));

                String searchEngine = state.searchEngine();
                Integer maxResults = state.maxSearchResults();
                Boolean fetchFullPage = state.fetchFullPage();

                log.info("Using search engine: {}, query: {}, max results: {}, fetch full page: {}", 
                    searchEngine, searchQuery, maxResults, fetchFullPage);

                // Execute search
                List<SearchResult> searchResults = searchEngineManager.search(
                    searchEngine, searchQuery, maxResults, fetchFullPage);

                log.info("Search completed, obtained {} results", searchResults.size());

                // Process search results
                List<String> webResults = new ArrayList<>();
                List<SearchResult> detailedResults = new ArrayList<>();

                for (SearchResult result : searchResults) {
                    // Add to simple results list
                    String simpleResult = String.format("[%s] %s - %s", 
                        result.getTitle(), result.getUrl(), result.getContent());
                    webResults.add(simpleResult);

                    // Add to detailed results list
                    detailedResults.add(result);

                    log.debug("Search result: {}", simpleResult);
                }

                // Return state updates
                return Map.of(
                    "web_search_results", webResults,
                    "detailed_search_results", detailedResults,
                    "current_node_start_time", nodeStart.get("current_node_start_time")
                );

            } catch (Exception e) {
                log.error("Web search failed", e);
                return state.setError("Web search failed: " + e.getMessage());
            }
    }
}
