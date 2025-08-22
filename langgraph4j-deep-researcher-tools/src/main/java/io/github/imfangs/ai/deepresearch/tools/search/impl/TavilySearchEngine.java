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
 * Tavily 搜索引擎实现
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
     * 初始化搜索引擎
     */
    private void initializeSearchEngine() {
        try {
            TavilyConfig tavilyConfig =
                researchConfig.getSearch().getTavily();
            
            if (!StringUtils.hasText(tavilyConfig.getApiKey())) {
                log.warn("Tavily API Key 未配置，Tavily 搜索引擎将不可用");
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

            log.info("Tavily 搜索引擎初始化成功");
        } catch (Exception e) {
            log.error("Tavily 搜索引擎初始化失败", e);
        }
    }

    @Override
    public List<SearchResult> search(String query, int maxResults, boolean fetchFullPage) {
        if (!isAvailable()) {
            log.warn("Tavily 搜索引擎不可用，返回空结果");
            return Collections.emptyList();
        }

        try {
            log.info("使用 Tavily 搜索: query={}, maxResults={}, fetchFullPage={}", 
                    query, maxResults, fetchFullPage);

            WebSearchRequest request = WebSearchRequest.builder()
                    .searchTerms(query)
                    .maxResults(maxResults)
                    .build();

            WebSearchResults results = tavilyWebSearchEngine.search(request);
            
            List<SearchResult> searchResults = results.results().stream()
                    .map(this::convertToSearchResult)
                    .collect(Collectors.toList());

            log.info("Tavily 搜索完成，获得 {} 个结果", searchResults.size());
            return searchResults;

        } catch (Exception e) {
            log.error("Tavily 搜索失败: query=" + query, e);
            return Collections.emptyList();
        }
    }

    /**
     * 转换 LangChain4j 搜索结果为内部格式
     */
    private SearchResult convertToSearchResult(WebSearchOrganicResult result) {
        Map<String, Object> metadata = new HashMap<>();
        
        // 添加额外元数据
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
     * 从元数据中提取相关性评分
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
                log.debug("无法解析评分: {}", scoreStr);
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
