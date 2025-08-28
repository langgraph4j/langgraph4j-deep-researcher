package io.github.imfangs.ai.deepresearch.api.state;

import io.github.imfangs.ai.deepresearch.api.dto.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Deep research state model
 * 
 * @author imfangs
 */
@Slf4j
public class ResearchState extends AgentState {

    /**
     * State schema definition
     */
    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
        Map.entry("research_topic", Channels.base(null, null)),
        Map.entry("search_query", Channels.base(null, null)),
        Map.entry("running_summary", Channels.base(null, null)),
        Map.entry("web_search_results", Channels.appender(() -> new ArrayList<String>())),
        Map.entry("sources_gathered", Channels.appender(() -> new ArrayList<String>())),
        Map.entry("detailed_search_results", Channels.appender(() -> new ArrayList<SearchResult>())),
        Map.entry("research_loop_count", Channels.base(null, null)),
        Map.entry("max_research_loops", Channels.base(null, null)),
        Map.entry("fetch_full_page", Channels.base(null, null)),
        Map.entry("max_search_results", Channels.base(null, null)),
        Map.entry("search_engine", Channels.base(null, null)),
        Map.entry("request_id", Channels.base(null, null)),
        Map.entry("user_id", Channels.base(null, null)),
        Map.entry("start_time", Channels.base(null, null)),
        Map.entry("current_node_start_time", Channels.base(null, null)),
        Map.entry("error_message", Channels.base(null, null)),
        Map.entry("success", Channels.base(null, null)),
        Map.entry("metadata", Channels.base(null, null))
    );

    /**
     * Constructor
     * 
     * @param initData Initialization data
     */
    public ResearchState(Map<String, Object> initData) {
        super(initData);
    }

    // === Convenience methods: Get state values ===

    /**
     * Get research topic
     */
    public Optional<String> researchTopic() {
        return this.value("research_topic");
    }

    /**
     * Get current search query
     */
    public Optional<String> searchQuery() {
        return this.value("search_query");
    }

    /**
     * Get running summary
     */
    public Optional<String> runningSummary() {
        return this.value("running_summary");
    }

    /**
     * Get web search results
     */
    @SuppressWarnings("unchecked")
    public List<String> webSearchResults() {
        return this.<List<String>>value("web_search_results").orElse(new ArrayList<>());
    }

    /**
     * Get collected source information
     */
    @SuppressWarnings("unchecked")
    public List<String> sourcesGathered() {
        return this.<List<String>>value("sources_gathered").orElse(new ArrayList<>());
    }

    /**
     * Get detailed search results
     */
    @SuppressWarnings("unchecked")
    public List<SearchResult> detailedSearchResults() {
        return this.<List<SearchResult>>value("detailed_search_results").orElse(new ArrayList<>());
    }

    /**
     * Get research loop count
     */
    public Integer researchLoopCount() {
        return this.<Integer>value("research_loop_count").orElse(0);
    }

    /**
     * Get maximum research loop count
     */
    public Integer maxResearchLoops() {
        return this.<Integer>value("max_research_loops").orElse(3);
    }

    /**
     * Whether to fetch full page content
     */
    public Boolean fetchFullPage() {
        return this.<Boolean>value("fetch_full_page").orElse(true);
    }

    /**
     * Get maximum search result count
     */
    public Integer maxSearchResults() {
        return this.<Integer>value("max_search_results").orElse(3);
    }

    /**
     * Get search engine type
     */
    public String searchEngine() {
        return this.<String>value("search_engine").orElse("tavily");
    }

    /**
     * Get request ID
     */
    public Optional<String> requestId() {
        return this.value("request_id");
    }

    /**
     * Get user ID
     */
    public Optional<String> userId() {
        return this.value("user_id");
    }

    /**
     * Get start time
     */
    public Optional<LocalDateTime> startTime() {
        return this.value("start_time");
    }

    /**
     * Get current node start time
     */
    public Optional<LocalDateTime> currentNodeStartTime() {
        return this.value("current_node_start_time");
    }

    /**
     * Get error message
     */
    public Optional<String> errorMessage() {
        return this.value("error_message");
    }

    /**
     * Whether successful
     */
    public Boolean success() {
        return this.<Boolean>value("success").orElse(true);
    }

    /**
     * Get metadata
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> metadata() {
        return this.<Map<String, Object>>value("metadata").orElse(Map.of());
    }

    // === Convenience methods: State operations ===

    /**
     * Check if maximum loop count is reached
     */
    public boolean hasReachedMaxLoops() {
        return this.researchLoopCount() >= this.maxResearchLoops();
    }

    /**
     * Increment loop count
     */
    public Map<String, Object> incrementLoopCount() {
        return Map.of("research_loop_count", this.researchLoopCount() + 1);
    }

    /**
     * Add search result
     */
    public Map<String, Object> addSearchResult(String result) {
        return Map.of("web_search_results", List.of(result));
    }

    /**
     * Add source information
     */
    public Map<String, Object> addSource(String source) {
        return Map.of("sources_gathered", List.of(source));
    }

    /**
     * Add detailed search result
     */
    public Map<String, Object> addDetailedSearchResult(SearchResult result) {
        return Map.of("detailed_search_results", List.of(result));
    }

    /**
     * Set error state
     */
    public Map<String, Object> setError(String errorMessage) {
        return Map.of(
            "success", false,
            "error_message", errorMessage
        );
    }

    /**
     * Mark node start
     */
    public Map<String, Object> markNodeStart() {
        return Map.of("current_node_start_time", LocalDateTime.now());
    }

    /**
     * Get current node execution duration (milliseconds)
     */
    public long getCurrentNodeDuration() {
        Optional<LocalDateTime> startTime = this.currentNodeStartTime();
        if (startTime.isEmpty()) {
            return 0;
        }
        return java.time.Duration.between(startTime.get(), LocalDateTime.now()).toMillis();
    }

    /**
     * Get total execution duration (milliseconds)
     */
    public long getTotalDuration() {
        Optional<LocalDateTime> startTime = this.startTime();
        if (startTime.isEmpty()) {
            return 0;
        }
        return java.time.Duration.between(startTime.get(), LocalDateTime.now()).toMillis();
    }

    /**
     * Create initial state
     */
    public static Map<String, Object> createInitialState(
            String researchTopic,
            String requestId,
            String userId,
            Integer maxLoops,
            String searchEngine,
            Integer maxResults,
            Boolean fetchFullPage) {
        
        return Map.of(
            "research_topic", researchTopic,
            "request_id", requestId,
            "user_id", userId,
            "max_research_loops", maxLoops != null ? maxLoops : 3,
            "search_engine", searchEngine != null ? searchEngine : "tavily",
            "max_search_results", maxResults != null ? maxResults : 3,
            "fetch_full_page", fetchFullPage != null ? fetchFullPage : true,
            "research_loop_count", 0,
            "success", true,
            "start_time", LocalDateTime.now()
        );
    }
}