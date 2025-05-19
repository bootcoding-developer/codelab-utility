package com.bootcoding.content.llm_generator.generator;//package com.bootcoding.content.llm_generator.generator;

import java.io.IOException;
import java.util.Map;

public interface ContentGenerationStrategy {
    Map<String, Object> generateContent(Map<String, Object> requestBody) throws IOException;
}



