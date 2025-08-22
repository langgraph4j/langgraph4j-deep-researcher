package io.github.imfangs.ai.deepresearch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 深度研究请求
 * 
 * @author imfangs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchRequest {

    /**
     * 研究主题 - 用户想要深入了解的主题
     */
    @NotBlank(message = "研究主题不能为空")
    @Size(max = 500, message = "研究主题不能超过500个字符")
    @JsonProperty("research_topic")
    private String researchTopic;

    /**
     * 最大研究循环次数 - 控制深度研究的迭代次数
     */
    @Min(value = 1, message = "研究循环次数至少为1次")
    @Max(value = 10, message = "研究循环次数最多为10次")
    @JsonProperty("max_research_loops")
    @Builder.Default
    private Integer maxResearchLoops = 3;

    /**
     * 搜索引擎类型 - 指定使用的搜索工具
     */
    @JsonProperty("search_engine")
    @Builder.Default
    private String searchEngine = "tavily";

    /**
     * 是否获取完整页面内容
     */
    @JsonProperty("fetch_full_page")
    @Builder.Default
    private Boolean fetchFullPage = true;

    /**
     * 每次搜索的最大结果数
     */
    @Min(value = 1, message = "搜索结果数至少为1")
    @Max(value = 10, message = "搜索结果数最多为10")
    @JsonProperty("max_search_results")
    @Builder.Default
    private Integer maxSearchResults = 3;

    /**
     * 请求ID - 用于追踪和日志记录
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 用户ID - 用于会话管理和个性化
     */
    @JsonProperty("user_id")
    private String userId;
}
