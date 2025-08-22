package io.github.imfangs.ai.deepresearch.core.controller;

import io.github.imfangs.ai.deepresearch.api.dto.ApiResponse;
import io.github.imfangs.ai.deepresearch.api.dto.ResearchRequest;
import io.github.imfangs.ai.deepresearch.api.dto.ResearchResponse;
import io.github.imfangs.ai.deepresearch.core.service.DeepResearchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * 深度研究控制器
 * 
 * 提供深度研究的REST API接口
 * 
 * @author imfangs
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/research")
@CrossOrigin(origins = "*")
public class DeepResearchController {

    private final DeepResearchService deepResearchService;

    @Autowired
    public DeepResearchController(DeepResearchService deepResearchService) {
        this.deepResearchService = deepResearchService;
    }

    /**
     * 执行深度研究
     * 
     * @param request 研究请求
     * @return 研究响应
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<ResearchResponse>> executeResearch(
            @Valid @RequestBody ResearchRequest request) {
        
        log.info("收到深度研究请求，主题: {}, 用户ID: {}", 
                request.getResearchTopic(), request.getUserId());
        
        try {
            ResearchResponse response = deepResearchService.executeResearch(request);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(response, "研究执行成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error(
                        500, 
                        response.getErrorMessage(), 
                        response.getRequestId()));
            }
            
        } catch (Exception e) {
            log.error("深度研究执行异常", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "系统内部错误: " + e.getMessage()));
        }
    }

    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("OK", "深度研究服务运行正常"));
    }

}