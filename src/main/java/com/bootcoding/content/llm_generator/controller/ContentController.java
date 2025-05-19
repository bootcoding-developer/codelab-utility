package com.bootcoding.content.llm_generator.controller;

import com.bootcoding.content.llm_generator.entity.ContentRequest;
import com.bootcoding.content.llm_generator.entity.ContentResponse;
import com.bootcoding.content.llm_generator.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/generate")
    public ContentResponse generateContent(@RequestBody ContentRequest request) throws IOException {
        Map<String, Object> generatedContent = contentService.generateContent(request);
        return new ContentResponse(generatedContent);
    }


}


