package io.github.imfangs.ai.deepresearch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Deep research response
 * 
 * @author imfangs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchResponse {

    /**
     * Request ID
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * Research topic
     */
    @JsonProperty("research_topic")
    private String researchTopic;

    /**
     * Final research report (Markdown format)
     */
    @JsonProperty("final_summary")
    private String finalSummary;

    /**
     * Actual number of research loops executed
     */
    @JsonProperty("actual_loops")
    private Integer actualLoops;

    /**
     * All source information collected during research
     */
    @JsonProperty("sources_gathered")
    private List<String> sourcesGathered;

    /**
     * Research start time
     */
    @JsonProperty("start_time")
    private LocalDateTime startTime;

    /**
     * Research end time
     */
    @JsonProperty("end_time")
    private LocalDateTime endTime;

    /**
     * Total duration (milliseconds)
     */
    @JsonProperty("duration_ms")
    private Long durationMs;

    /**
     * Whether successfully completed
     */
    @JsonProperty("success")
    private Boolean success;

    /**
     * Error message (if failed)
     */
    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * Research status
     */
    @JsonProperty("status")
    private ResearchStatus status;

    /**
     * Research status enumeration
     */
    public enum ResearchStatus {
        PENDING,     // Waiting to start
        IN_PROGRESS, // In progress
        COMPLETED,   // Completed
        FAILED,      // Failed
        CANCELLED    // Cancelled
    }
}
