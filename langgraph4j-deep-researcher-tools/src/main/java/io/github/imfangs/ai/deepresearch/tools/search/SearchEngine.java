package io.github.imfangs.ai.deepresearch.tools.search;

import io.github.imfangs.ai.deepresearch.api.dto.SearchResult;

import java.util.List;

/**
 * 搜索引擎接口
 * 
 * @author imfangs
 */
public interface SearchEngine {

    /**
     * 执行搜索
     *
     * @param query 搜索查询
     * @param maxResults 最大结果数
     * @param fetchFullPage 是否获取完整页面内容
     * @return 搜索结果列表
     */
    List<SearchResult> search(String query, int maxResults, boolean fetchFullPage);

    /**
     * 获取搜索引擎名称
     *
     * @return 搜索引擎名称
     */
    String getEngineName();

    /**
     * 检查搜索引擎是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}
