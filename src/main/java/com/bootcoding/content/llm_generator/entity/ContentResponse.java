package com.bootcoding.content.llm_generator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor // Add this annotation to generate a constructor
public class ContentResponse {
    //private String status;
    private Map<String, Object> data;
}

