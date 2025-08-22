package io.github.imfangs.ai.deepresearch.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 搜索配置
 */
@Data
public class SearchConfig {

    /**
     * 默认搜索引擎
     */
    @NotBlank(message = "默认搜索引擎不能为空")
    private String defaultEngine = "tavily";

    /**
     * Tavily API 配置
     */
    @Valid
    private TavilyConfig tavily = new TavilyConfig();

    // Getter 方法
    public TavilyConfig getTavily() {
        return tavily;
    }

}
