package io.github.imfangs.ai.deepresearch.api.state;

import io.github.imfangs.ai.deepresearch.api.dto.SearchResult;
import org.bsc.langgraph4j.serializer.Serializer;
import org.bsc.langgraph4j.serializer.std.ObjectStreamStateSerializer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 研究状态序列化器
 * 
 * @author imfangs
 */
public class ResearchStateSerializer extends ObjectStreamStateSerializer<ResearchState> {

    public ResearchStateSerializer() {
        super(ResearchState::new);
        
        // 注册自定义类型的序列化器
        mapper().register(SearchResult.class, new SearchResultSerializer());
        mapper().register(LocalDateTime.class, new LocalDateTimeSerializer());
    }
    
    /**
     * SearchResult 序列化器
     * 
     * 为 SearchResult 提供明确的序列化支持，确保所有字段正确序列化
     */
    private static class SearchResultSerializer implements Serializer<SearchResult> {
        
        @Override
        public void write(SearchResult object, ObjectOutput out) throws IOException {
            // 按字段顺序写入
            out.writeObject(object.getTitle());
            out.writeObject(object.getUrl());
            out.writeObject(object.getContent());
            out.writeObject(object.getRawContent());
            out.writeObject(object.getScore());
            out.writeObject(object.getMetadata());
            out.writeObject(object.getSourceEngine());
        }

        @Override
        public SearchResult read(ObjectInput in) throws IOException, ClassNotFoundException {
            // 按相同顺序读取
            String title = (String) in.readObject();
            String url = (String) in.readObject();
            String content = (String) in.readObject();
            String rawContent = (String) in.readObject();
            Double score = (Double) in.readObject();
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) in.readObject();
            String sourceEngine = (String) in.readObject();
            
            return SearchResult.builder()
                    .title(title)
                    .url(url)
                    .content(content)
                    .rawContent(rawContent)
                    .score(score)
                    .metadata(metadata)
                    .sourceEngine(sourceEngine)
                    .build();
        }
    }
    
    /**
     * LocalDateTime 序列化器
     * 
     * 为 LocalDateTime 提供序列化支持，用于时间相关字段
     */
    private static class LocalDateTimeSerializer implements Serializer<LocalDateTime> {
        
        @Override
        public void write(LocalDateTime object, ObjectOutput out) throws IOException {
            // 转换为 ISO 字符串进行序列化
            out.writeObject(object.toString());
        }

        @Override
        public LocalDateTime read(ObjectInput in) throws IOException, ClassNotFoundException {
            // 从 ISO 字符串恢复
            String isoString = (String) in.readObject();
            return LocalDateTime.parse(isoString);
        }
    }
}
