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
 * 搜索引擎管理器
 * 负责管理多个搜索引擎，提供统一的搜索接口
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
                    (existing, replacement) -> existing  // 如果有重复名称，保留现有的
                ));
        
        log.info("搜索引擎管理器初始化完成，可用引擎: {}", 
                getAvailableEngines().stream()
                        .map(SearchEngine::getEngineName)
                        .collect(Collectors.joining(", ")));
    }

    /**
     * 使用指定搜索引擎执行搜索
     *
     * @param engineName 搜索引擎名称
     * @param query 搜索查询
     * @param maxResults 最大结果数
     * @param fetchFullPage 是否获取完整页面内容
     * @return 搜索结果列表
     */
    public List<SearchResult> search(String engineName, String query, int maxResults, boolean fetchFullPage) {
        SearchEngine engine = getSearchEngine(engineName);
        if (engine == null) {
            log.error("未找到搜索引擎: {}", engineName);
            return Collections.emptyList();
        }

        if (!engine.isAvailable()) {
            log.warn("搜索引擎 {} 当前不可用", engineName);
            return Collections.emptyList();
        }

        return engine.search(query, maxResults, fetchFullPage);
    }

    /**
     * 使用默认搜索引擎执行搜索
     *
     * @param query 搜索查询
     * @param maxResults 最大结果数
     * @param fetchFullPage 是否获取完整页面内容
     * @return 搜索结果列表
     */
    public List<SearchResult> searchWithDefault(String query, int maxResults, boolean fetchFullPage) {
        String defaultEngine = researchConfig.getSearch().getDefaultEngine();
        return search(defaultEngine, query, maxResults, fetchFullPage);
    }

    /**
     * 使用默认搜索引擎执行搜索（使用配置中的默认参数）
     *
     * @param query 搜索查询
     * @return 搜索结果列表
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
     * 获取指定名称的搜索引擎
     *
     * @param engineName 搜索引擎名称
     * @return 搜索引擎实例，如果不存在则返回null
     */
    public SearchEngine getSearchEngine(String engineName) {
        return searchEngines.get(engineName);
    }

    /**
     * 获取所有可用的搜索引擎
     *
     * @return 可用搜索引擎列表
     */
    public List<SearchEngine> getAvailableEngines() {
        return searchEngines.values().stream()
                .filter(SearchEngine::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有搜索引擎名称
     *
     * @return 搜索引擎名称列表
     */
    public Set<String> getAllEngineNames() {
        return searchEngines.keySet();
    }

    /**
     * 获取所有可用搜索引擎名称
     *
     * @return 可用搜索引擎名称列表
     */
    public List<String> getAvailableEngineNames() {
        return getAvailableEngines().stream()
                .map(SearchEngine::getEngineName)
                .collect(Collectors.toList());
    }

    /**
     * 检查指定搜索引擎是否可用
     *
     * @param engineName 搜索引擎名称
     * @return 是否可用
     */
    public boolean isEngineAvailable(String engineName) {
        SearchEngine engine = getSearchEngine(engineName);
        return engine != null && engine.isAvailable();
    }

    /**
     * 去重和格式化搜索结果
     *
     * @param searchResults 搜索结果列表
     * @param maxTokensPerSource 每个源的最大token数
     * @return 格式化的搜索结果字符串
     */
    public String formatSearchResults(List<SearchResult> searchResults, int maxTokensPerSource) {
        if (searchResults == null || searchResults.isEmpty()) {
            return "未找到相关搜索结果";
        }

        // 按URL去重
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

            // 如果有原始内容且长度超过限制，则截断
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
