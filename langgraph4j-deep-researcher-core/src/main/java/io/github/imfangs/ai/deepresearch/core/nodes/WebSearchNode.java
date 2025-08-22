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
 * Web搜索节点
 * 
 * 负责执行Web搜索并收集结果
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
                log.info("🌐 开始Web搜索");

                // 标记节点开始
                Map<String, Object> nodeStart = state.markNodeStart();

                String searchQuery = state.searchQuery()
                    .orElseThrow(() -> new IllegalStateException("缺少搜索查询"));

                String searchEngine = state.searchEngine();
                Integer maxResults = state.maxSearchResults();
                Boolean fetchFullPage = state.fetchFullPage();

                log.info("使用搜索引擎: {}, 查询: {}, 最大结果数: {}, 获取完整页面: {}", 
                    searchEngine, searchQuery, maxResults, fetchFullPage);

                // 执行搜索
                List<SearchResult> searchResults = searchEngineManager.search(
                    searchEngine, searchQuery, maxResults, fetchFullPage);

                log.info("搜索完成，获得 {} 个结果", searchResults.size());

                // 处理搜索结果
                List<String> webResults = new ArrayList<>();
                List<SearchResult> detailedResults = new ArrayList<>();

                for (SearchResult result : searchResults) {
                    // 添加到简单结果列表
                    String simpleResult = String.format("[%s] %s - %s", 
                        result.getTitle(), result.getUrl(), result.getContent());
                    webResults.add(simpleResult);

                    // 添加到详细结果列表
                    detailedResults.add(result);

                    log.debug("搜索结果: {}", simpleResult);
                }

                // 返回状态更新
                return Map.of(
                    "web_search_results", webResults,
                    "detailed_search_results", detailedResults,
                    "current_node_start_time", nodeStart.get("current_node_start_time")
                );

            } catch (Exception e) {
                log.error("Web搜索失败", e);
                return state.setError("Web搜索失败: " + e.getMessage());
            }
    }
}
