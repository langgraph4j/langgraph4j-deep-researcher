package io.github.imfangs.ai.deepresearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Langgraph4j 深度研究应用程序主启动类
 * 
 * @author imfangs
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "io.github.imfangs.ai.deepresearch")
@EnableConfigurationProperties
public class DeepResearchApplication {

    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(DeepResearchApplication.class, args);
            
            Environment env = context.getEnvironment();
            String protocol = "http";
            if (env.getProperty("server.ssl.key-store") != null) {
                protocol = "https";
            }
            
            String serverPort = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "");
            String hostAddress = "localhost";
            
            try {
                hostAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.warn("无法获取主机地址", e);
            }
            
            log.info("""
                
                🚀 Langgraph4j Deep Researcher 启动成功！
                
                ===================================
                🌐 应用访问地址:
                   Local:    {}://localhost:{}{}
                   External: {}://{}:{}{}
                   
                📚 API 文档:
                   健康检查: {}/api/v1/research/health
                   研究接口: {}/api/v1/research/execute
                   
                🔧 配置信息:
                   Profile: {}
                   Java版本: {}
                ===================================
                """,
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol + "://localhost:" + serverPort + contextPath,
                protocol + "://localhost:" + serverPort + contextPath,
                String.join(",", env.getActiveProfiles()),
                System.getProperty("java.version")
            );
            
        } catch (Exception e) {
            log.error("应用启动失败", e);
            System.exit(1);
        }
    }
}
