package io.github.imfangs.ai.deepresearch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一API响应包装类
 * 
 * @param <T> 响应数据类型
 * @author imfangs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 响应状态码
     */
    @JsonProperty("code")
    @Builder.Default
    private Integer code = 200;

    /**
     * 响应消息
     */
    @JsonProperty("message")
    @Builder.Default
    private String message = "success";

    /**
     * 响应数据
     */
    @JsonProperty("data")
    private T data;

    /**
     * 请求ID
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 响应时间戳
     */
    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 请求耗时（毫秒）
     */
    @JsonProperty("duration_ms")
    private Long durationMs;

    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    /**
     * 创建成功响应（带请求ID）
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
     * 创建失败响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 创建失败响应（带请求ID）
     */
    public static <T> ApiResponse<T> error(Integer code, String message, String requestId) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .requestId(requestId)
                .build();
    }

    /**
     * 创建参数错误响应
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(400, message);
    }

    /**
     * 创建服务器错误响应
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return error(500, message);
    }
}
