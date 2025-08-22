package io.github.imfangs.ai.deepresearch.config;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Research Model 配置
 */
@Data
public class ResearchModelConfig {

    /**
     * App ID
     */
    @NotBlank(message = "API KEY 不能为空")
    private String apiKey;

    /**
     * API URL
     */
    @NotBlank(message = "API URL 不能为空")
    private String apiUrl;

    /**
     * 默认使用的模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String modelName = "gpt-4o-mini";

    /**
     * 模型温度参数
     */
    @DecimalMin(value = "0.0", message = "温度参数不能小于0")
    @DecimalMax(value = "2.0", message = "温度参数不能大于2")
    private Double temperature = 0.1;

    /**
     * 最大token数
     */
    @Min(value = 100, message = "最大token数至少为100")
    @Max(value = 16000, message = "最大token数不能超过16000")
    private Integer maxTokens = 4096;

    /**
     * 是否启用请求日志
     */
    private Boolean logRequests = true;

    /**
     * 是否启用响应日志
     */
    private Boolean logResponses = true;
}