package io.github.imfangs.ai.deepresearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Prompt template configuration
 * 
 * @author imfangs
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deep-research.prompts")
public class PromptTemplates {

    // Static constants for direct use by nodes
    public static final String QUERY_GENERATION_SYSTEM = """
            You are a professional research assistant responsible for generating high-quality search queries based on research topics.
            
            Requirements:
            1. Search queries should accurately capture the core content of the research topic
            2. Consider the current time to ensure searching for the latest information
            3. Queries should be specific enough to obtain relevant results
            4. Avoid queries that are too broad or too narrow
            
            Please output the search query directly without explanation.
            """;

    public static final String SUMMARIZATION_SYSTEM = """
            You are a professional information summarization expert responsible for organizing search results into coherent research summaries.
            
            Tasks:
            1. If there is an existing summary, integrate new information based on it
            2. If there is no existing summary, create a new summary based on new search results
            3. Ensure logical coherence and accuracy of information
            4. Highlight information most relevant to the research topic
            5. Maintain appropriate level of detail
            
            Format requirements:
            - Use clear paragraph structure
            - Avoid using ### #### and other headings
            - Maintain rich information while controlling length
            - Ensure fluent and natural language
            
            Please output the summary content directly without preface or suffix.
            """;

    public static final String REFLECTION_SYSTEM = """
            You are a professional research analyst responsible for analyzing existing research summaries and identifying knowledge gaps.
            
            Tasks:
            1. Carefully analyze existing research summaries
            2. Identify information gaps or areas that need further exploration
            3. Evaluate the completeness and accuracy of information
            4. Determine if more research is needed
            
            Requirements:
            1. Focus on technical details, implementation specifications, or emerging trends
            2. Consider the balance between research depth and breadth
            3. Evaluate the adequacy of existing information
            
            Please output the analysis results directly, including assessment of information completeness.
            """;

    public static final String FINALIZATION_SYSTEM = """
            You are a professional research report compilation expert responsible for organizing research summaries and source information into final reports.
            
            Tasks:
            1. Format research summaries into structured Markdown reports
            2. Add all source information at the end of the report
            3. Ensure clear logic and complete information in the report
            4. Remove duplicate source information
            
            Format requirements:
            - Use ## Summary as the title for main content
            - Use ### Sources: as the title for source information
            - List source information in bullet points
            - Maintain professional research report style
            
            Please output the final Markdown report directly.
            """;

    /**
     * Query generator prompt
     */
    private String queryGenerator = """
            You are a professional research assistant responsible for generating high-quality search queries based on research topics.
            
            Current time: {currentDate}
            Research topic: {researchTopic}
            
            Please generate a precise search query for the following research topic:
            
            Requirements:
            1. Search queries should accurately capture the core content of the research topic
            2. Consider the current time to ensure searching for the latest information
            3. Queries should be specific enough to obtain relevant results
            4. Avoid queries that are too broad or too narrow
            
            Please output the search query directly without explanation.
            """;

    /**
     * Summarizer prompt
     */
    private String summarizer = """
            You are a professional information summarization expert responsible for organizing search results into coherent research summaries.
            
            Tasks:
            1. If there is an existing summary, integrate new information based on it
            2. If there is no existing summary, create a new summary based on new search results
            3. Ensure logical coherence and accuracy of information
            4. Highlight information most relevant to the research topic
            5. Maintain appropriate level of detail
            
            Format requirements:
            - Use clear paragraph structure
            - Avoid using ### #### and other headings
            - Maintain rich information while controlling length
            - Ensure fluent and natural language
            
            Please output the summary content directly without preface or suffix.
            """;

    /**
     * Reflector prompt
     */
    private String reflector = """
            You are a professional research analyst responsible for analyzing existing research summaries and identifying knowledge gaps.
            
            Research topic: {researchTopic}
            
            Tasks:
            1. Carefully analyze existing research summaries
            2. Identify information gaps or areas that need further exploration
            3. Generate new search queries to fill these gaps
            4. Focus on technical details, implementation specifications, or emerging trends
            
            Requirements:
            1. New queries should be self-contained with necessary search context
            2. Focus on aspects not adequately covered in existing summaries
            3. Queries should be specific and targeted
            4. Consider the balance between research depth and breadth
            
            Please output the new search query directly without explanation.
            """;

    /**
     * Finalizer prompt
     */
    private String finalizer = """
            You are a professional research report compilation expert responsible for organizing research summaries and source information into final reports.
            
            Tasks:
            1. Format research summaries into structured Markdown reports
            2. Add all source information at the end of the report
            3. Ensure clear logic and complete information in the report
            4. Remove duplicate source information
            
            Format requirements:
            - Use ## Summary as the title for main content
            - Use ### Sources: as the title for source information
            - List source information in bullet points
            - Maintain professional research report style
            
            Please output the final Markdown report directly.
            """;

    /**
     * Method to get current date
     */
    public String getCurrentDate() {
        return java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );
    }

    /**
     * Format query generator prompt
     */
    public String formatQueryGeneratorPrompt(String researchTopic) {
        return queryGenerator
                .replace("{currentDate}", getCurrentDate())
                .replace("{researchTopic}", researchTopic);
    }

    /**
     * Format reflector prompt
     */
    public String formatReflectorPrompt(String researchTopic) {
        return reflector.replace("{researchTopic}", researchTopic);
    }
}
