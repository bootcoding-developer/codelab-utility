package com.bootcoding.content.llm_generator.service;

import com.bootcoding.content.llm_generator.entity.ContentRequest;
import com.bootcoding.content.llm_generator.factory.ContentGeneratorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentGeneratorFactory contentGeneratorFactory;

    public Map<String, Object> generateContent(ContentRequest request) throws IOException {
        return contentGeneratorFactory.getStrategy(request.getContentType()).generateContent(request.getDetails());
    }
}






