package io.github.imfangs.ai.deepresearch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 搜索结果数据传输对象
 * 
 * @author imfangs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    /**
     * 标题
     */
    @JsonProperty("title")
    private String title;

    /**
     * URL
     */
    @JsonProperty("url")
    private String url;

    /**
     * 摘要内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 原始页面内容（如果启用了获取完整页面）
     */
    @JsonProperty("raw_content")
    private String rawContent;

    /**
     * 相关性评分
     */
    @JsonProperty("score")
    private Double score;

    /**
     * 搜索引擎返回的额外元数据
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * 源搜索引擎
     */
    @JsonProperty("source_engine")
    private String sourceEngine;
}
