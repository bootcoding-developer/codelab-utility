package com.bootcoding.content.llm_generator.factory;

import com.bootcoding.content.llm_generator.generator.ContentGenerationStrategy;
import com.bootcoding.content.llm_generator.generator.NotesGenerationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentGeneratorFactory {

    private final NotesGenerationStrategy notesGenerationStrategy;


    public ContentGenerationStrategy getStrategy(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "notes" -> notesGenerationStrategy;
           // case "coding" -> codingQuestionGenerationStrategy;
            default -> throw new IllegalArgumentException("Invalid content type: " + contentType);
        };
    }
}

