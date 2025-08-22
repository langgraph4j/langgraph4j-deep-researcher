package io.github.imfangs.ai.deepresearch.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 研究流程配置
 */
@Data
public class FlowConfig {

    /**
     * 默认最大研究循环次数
     */
    @Min(value = 1, message = "研究循环次数至少为1次")
    @Max(value = 10, message = "研究循环次数最多为10次")
    private Integer defaultMaxLoops = 3;

    /**
     * 默认每次搜索的最大结果数
     */
    @Min(value = 1, message = "搜索结果数至少为1")
    @Max(value = 10, message = "搜索结果数最多为10")
    private Integer defaultMaxSearchResults = 3;

    /**
     * 是否默认获取完整页面内容
     */
    private Boolean defaultFetchFullPage = true;

    /**
     * 每个源的最大token数
     */
    @Min(value = 100, message = "每个源的最大token数至少为100")
    @Max(value = 2000, message = "每个源的最大token数不能超过2000")
    private Integer maxTokensPerSource = 1000;

    /**
     * 字符到token的转换比例
     */
    @Min(value = 2, message = "字符到token转换比例至少为2")
    @Max(value = 10, message = "字符到token转换比例不能超过10")
    private Integer charsPerToken = 4;
}