package io.github.imfangs.ai.deepresearch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 深度研究响应
 * 
 * @author imfangs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchResponse {

    /**
     * 请求ID
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 研究主题
     */
    @JsonProperty("research_topic")
    private String researchTopic;

    /**
     * 最终研究报告（Markdown格式）
     */
    @JsonProperty("final_summary")
    private String finalSummary;

    /**
     * 实际执行的研究循环次数
     */
    @JsonProperty("actual_loops")
    private Integer actualLoops;

    /**
     * 研究过程中收集的所有源信息
     */
    @JsonProperty("sources_gathered")
    private List<String> sourcesGathered;

    /**
     * 研究开始时间
     */
    @JsonProperty("start_time")
    private LocalDateTime startTime;

    /**
     * 研究结束时间
     */
    @JsonProperty("end_time")
    private LocalDateTime endTime;

    /**
     * 总耗时（毫秒）
     */
    @JsonProperty("duration_ms")
    private Long durationMs;

    /**
     * 是否成功完成
     */
    @JsonProperty("success")
    private Boolean success;

    /**
     * 错误信息（如果失败）
     */
    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * 研究状态
     */
    @JsonProperty("status")
    private ResearchStatus status;

    /**
     * 研究状态枚举
     */
    public enum ResearchStatus {
        PENDING,     // 等待开始
        IN_PROGRESS, // 进行中
        COMPLETED,   // 已完成
        FAILED,      // 失败
        CANCELLED    // 已取消
    }
}
