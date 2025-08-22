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
 * 深度研究状态模型
 * 
 * @author imfangs
 */
@Slf4j
public class ResearchState extends AgentState {

    /**
     * 状态Schema定义
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
     * 构造函数
     * 
     * @param initData 初始化数据
     */
    public ResearchState(Map<String, Object> initData) {
        super(initData);
    }

    // === 便利方法：获取状态值 ===

    /**
     * 获取研究主题
     */
    public Optional<String> researchTopic() {
        return this.value("research_topic");
    }

    /**
     * 获取当前搜索查询
     */
    public Optional<String> searchQuery() {
        return this.value("search_query");
    }

    /**
     * 获取运行中的总结
     */
    public Optional<String> runningSummary() {
        return this.value("running_summary");
    }

    /**
     * 获取Web搜索结果
     */
    @SuppressWarnings("unchecked")
    public List<String> webSearchResults() {
        return this.<List<String>>value("web_search_results").orElse(new ArrayList<>());
    }

    /**
     * 获取收集的源信息
     */
    @SuppressWarnings("unchecked")
    public List<String> sourcesGathered() {
        return this.<List<String>>value("sources_gathered").orElse(new ArrayList<>());
    }

    /**
     * 获取详细搜索结果
     */
    @SuppressWarnings("unchecked")
    public List<SearchResult> detailedSearchResults() {
        return this.<List<SearchResult>>value("detailed_search_results").orElse(new ArrayList<>());
    }

    /**
     * 获取研究循环计数
     */
    public Integer researchLoopCount() {
        return this.<Integer>value("research_loop_count").orElse(0);
    }

    /**
     * 获取最大研究循环次数
     */
    public Integer maxResearchLoops() {
        return this.<Integer>value("max_research_loops").orElse(3);
    }

    /**
     * 是否获取完整页面内容
     */
    public Boolean fetchFullPage() {
        return this.<Boolean>value("fetch_full_page").orElse(true);
    }

    /**
     * 获取最大搜索结果数
     */
    public Integer maxSearchResults() {
        return this.<Integer>value("max_search_results").orElse(3);
    }

    /**
     * 获取搜索引擎类型
     */
    public String searchEngine() {
        return this.<String>value("search_engine").orElse("tavily");
    }

    /**
     * 获取请求ID
     */
    public Optional<String> requestId() {
        return this.value("request_id");
    }

    /**
     * 获取用户ID
     */
    public Optional<String> userId() {
        return this.value("user_id");
    }

    /**
     * 获取开始时间
     */
    public Optional<LocalDateTime> startTime() {
        return this.value("start_time");
    }

    /**
     * 获取当前节点开始时间
     */
    public Optional<LocalDateTime> currentNodeStartTime() {
        return this.value("current_node_start_time");
    }

    /**
     * 获取错误信息
     */
    public Optional<String> errorMessage() {
        return this.value("error_message");
    }

    /**
     * 是否成功
     */
    public Boolean success() {
        return this.<Boolean>value("success").orElse(true);
    }

    /**
     * 获取元数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> metadata() {
        return this.<Map<String, Object>>value("metadata").orElse(Map.of());
    }

    // === 便利方法：状态操作 ===

    /**
     * 检查是否达到最大循环次数
     */
    public boolean hasReachedMaxLoops() {
        return this.researchLoopCount() >= this.maxResearchLoops();
    }

    /**
     * 增加循环计数
     */
    public Map<String, Object> incrementLoopCount() {
        return Map.of("research_loop_count", this.researchLoopCount() + 1);
    }

    /**
     * 添加搜索结果
     */
    public Map<String, Object> addSearchResult(String result) {
        return Map.of("web_search_results", List.of(result));
    }

    /**
     * 添加源信息
     */
    public Map<String, Object> addSource(String source) {
        return Map.of("sources_gathered", List.of(source));
    }

    /**
     * 添加详细搜索结果
     */
    public Map<String, Object> addDetailedSearchResult(SearchResult result) {
        return Map.of("detailed_search_results", List.of(result));
    }

    /**
     * 设置错误状态
     */
    public Map<String, Object> setError(String errorMessage) {
        return Map.of(
            "success", false,
            "error_message", errorMessage
        );
    }

    /**
     * 标记节点开始
     */
    public Map<String, Object> markNodeStart() {
        return Map.of("current_node_start_time", LocalDateTime.now());
    }

    /**
     * 获取当前节点执行时长（毫秒）
     */
    public long getCurrentNodeDuration() {
        Optional<LocalDateTime> startTime = this.currentNodeStartTime();
        if (startTime.isEmpty()) {
            return 0;
        }
        return java.time.Duration.between(startTime.get(), LocalDateTime.now()).toMillis();
    }

    /**
     * 获取总执行时长（毫秒）
     */
    public long getTotalDuration() {
        Optional<LocalDateTime> startTime = this.startTime();
        if (startTime.isEmpty()) {
            return 0;
        }
        return java.time.Duration.between(startTime.get(), LocalDateTime.now()).toMillis();
    }

    /**
     * 创建初始状态
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