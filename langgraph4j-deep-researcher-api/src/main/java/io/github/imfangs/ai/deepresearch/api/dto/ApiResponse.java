package io.github.imfangs.ai.deepresearch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unified API response wrapper class
 * 
 * @param <T> Response data type
 * @author imfangs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * Response status code
     */
    @JsonProperty("code")
    @Builder.Default
    private Integer code = 200;

    /**
     * Response message
     */
    @JsonProperty("message")
    @Builder.Default
    private String message = "success";

    /**
     * Response data
     */
    @JsonProperty("data")
    private T data;

    /**
     * Request ID
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * Response timestamp
     */
    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Request duration (milliseconds)
     */
    @JsonProperty("duration_ms")
    private Long durationMs;

    /**
     * Create success response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    /**
     * Create success response (with request ID)
     */
    public static <T> ApiResponse<T> success(T data, String requestId) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .requestId(requestId)
                .build();
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * Create error response (with request ID)
     */
    public static <T> ApiResponse<T> error(Integer code, String message, String requestId) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .requestId(requestId)
                .build();
    }

    /**
     * Create bad request response
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(400, message);
    }

    /**
     * Create server error response
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return error(500, message);
    }
}
