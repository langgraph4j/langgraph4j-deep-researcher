package io.github.imfangs.ai.deepresearch;

import io.github.imfangs.ai.deepresearch.api.dto.ResearchRequest;
import io.github.imfangs.ai.deepresearch.config.ResearchConfig;
import io.github.imfangs.ai.deepresearch.core.controller.DeepResearchController;
import io.github.imfangs.ai.deepresearch.core.service.DeepResearchService;
import io.github.imfangs.ai.deepresearch.tools.search.SearchEngineManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Langgraph4j Deep Researcher 应用程序集成测试
 * 
 * @author imfangs
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "deep-research.model.app-id=test-api-key",
        "deep-research.model.api-url=http://localhost:8080/mock-api",
        "deep-research.search.tavily.api-key=test-api-key"
})
class DeepResearchApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DeepResearchController controller;

    @Autowired
    private DeepResearchService service;

    @Autowired
    private SearchEngineManager searchEngineManager;

    @Autowired
    private ResearchConfig researchConfig;

    @Test
    void contextLoads() {
        // 验证应用上下文正确加载
        assertThat(controller).isNotNull();
        assertThat(service).isNotNull();
        assertThat(searchEngineManager).isNotNull();
        assertThat(researchConfig).isNotNull();
    }

    @Test
    void healthEndpointReturnsOk() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/research/health", 
                String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("OK");
    }

    @Test
    void configurationIsLoadedCorrectly() {
        assertThat(researchConfig.getModel().getApiKey()).isEqualTo("test-api-key");
        assertThat(researchConfig.getModel().getApiUrl()).isEqualTo("http://localhost:8080/mock-api");
        assertThat(researchConfig.getSearch().getTavily().getApiKey()).isEqualTo("test-api-key");
        assertThat(researchConfig.getFlow().getDefaultMaxLoops()).isEqualTo(3);
    }

    @Test
    void searchEngineManagerInitializesCorrectly() {
        assertThat(searchEngineManager.getAllEngineNames()).contains("tavily");
        assertThat(searchEngineManager.getSearchEngine("tavily")).isNotNull();
    }

    @Test
    void researchRequestValidationWorks() {
        // 测试无效请求
        ResearchRequest invalidRequest = ResearchRequest.builder()
                .researchTopic("") // 空主题
                .maxResearchLoops(0) // 无效循环次数
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/research/execute",
                invalidRequest,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void validResearchRequestStructure() {
        // 测试有效请求结构（不实际执行，避免依赖外部服务）
        ResearchRequest validRequest = ResearchRequest.builder()
                .researchTopic("Test research topic")
                .maxResearchLoops(2)
                .searchEngine("tavily")
                .maxSearchResults(2)
                .fetchFullPage(true)
                .build();

        // 验证请求对象构建正确
        assertThat(validRequest.getResearchTopic()).isEqualTo("Test research topic");
        assertThat(validRequest.getMaxResearchLoops()).isEqualTo(2);
        assertThat(validRequest.getSearchEngine()).isEqualTo("tavily");
    }
}
