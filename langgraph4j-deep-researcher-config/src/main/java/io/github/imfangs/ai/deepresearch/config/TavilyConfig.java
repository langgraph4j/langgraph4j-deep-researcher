package io.github.imfangs.ai.deepresearch.config;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Tavily 配置
 */
@Data
public class TavilyConfig {
    /**
     * Tavily API Key
     */
    private String apiKey;

    /**
     * API 基础URL
     */
    private String baseUrl = "https://api.tavily.com/";

    /**
     * 搜索深度
     */
    private String searchDepth = "advanced";

    /**
     * 是否包含答案
     */
    private Boolean includeAnswer = false;

    /**
     * 是否包含原始内容
     */
    private Boolean includeRawContent = true;

    /**
     * 请求超时时间（秒）
     */
    @Min(value = 5, message = "超时时间至少为5秒")
    @Max(value = 300, message = "超时时间不能超过300秒")
    private Integer timeoutSeconds = 30;
}