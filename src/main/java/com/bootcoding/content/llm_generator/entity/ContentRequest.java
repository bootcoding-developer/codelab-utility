package com.bootcoding.content.llm_generator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentRequest {
    private String contentType;             // e.g., "notes", "coding", "assignment"
    private Map<String, Object> details;    // e.g., { "subject": "Java", "topic": "OOPs" }
}
