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
 * Research state serializer
 * 
 * @author imfangs
 */
public class ResearchStateSerializer extends ObjectStreamStateSerializer<ResearchState> {

    public ResearchStateSerializer() {
        super(ResearchState::new);
        
        // Register custom type serializers
        mapper().register(SearchResult.class, new SearchResultSerializer());
        mapper().register(LocalDateTime.class, new LocalDateTimeSerializer());
    }
    
    /**
     * SearchResult serializer
     * 
     * Provides explicit serialization support for SearchResult, ensuring all fields are correctly serialized
     */
    private static class SearchResultSerializer implements Serializer<SearchResult> {
        
        @Override
        public void write(SearchResult object, ObjectOutput out) throws IOException {
            // Write in field order
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
            // Read in the same order
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
     * LocalDateTime serializer
     * 
     * Provides serialization support for LocalDateTime, used for time-related fields
     */
    private static class LocalDateTimeSerializer implements Serializer<LocalDateTime> {
        
        @Override
        public void write(LocalDateTime object, ObjectOutput out) throws IOException {
            // Convert to ISO string for serialization
            out.writeObject(object.toString());
        }

        @Override
        public LocalDateTime read(ObjectInput in) throws IOException, ClassNotFoundException {
            // Restore from ISO string
            String isoString = (String) in.readObject();
            return LocalDateTime.parse(isoString);
        }
    }
}
