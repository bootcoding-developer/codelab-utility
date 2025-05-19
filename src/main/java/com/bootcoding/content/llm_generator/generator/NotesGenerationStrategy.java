package com.bootcoding.content.llm_generator.generator;

import com.bootcoding.content.llm_generator.APIConfig.ApiConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class NotesGenerationStrategy implements ContentGenerationStrategy {

    private final ApiConfiguration apiConfiguration;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> generateContent(Map<String, Object> requestBody) throws IOException {
        String subject = (String) requestBody.get("subject");
        String topic = (String) requestBody.get("topic");

        String systemPrompt = generateSystemPrompt();
        String userInstruction = generateUserInstruction(subject, topic);
        String requestPayload = buildRequestPayload(systemPrompt, userInstruction);

        String response = callApi(requestPayload);
        return processApiResponse(response, subject); // Pass subject for dynamic collection name
    }

    // Generate system prompt
    private String generateSystemPrompt() {
        return "Generate structured study notes.";
    }

    // Generate user instructions
    private String generateUserInstruction(String subject, String topic) {
        return String.format(
                "Generate detailed study notes as like w3schools  website in Map<String, Object> format on the topic '%s' under the subject '%s' and image ad video url must be fill then content from section must be more than 15 lines. "
                        + "Ensure that the output is a strictly formatted JSON without any * :\n\n"
                        + "[ { \"contentId\": \"\", \"heading\": \"Main heading\", \"description\": \"Brief description\", \"tags\": [\"Java\", \"Collections\"],"
                        + "  \"sections\": [ { \"title\": \"Section Title\", \"content\": \"Detailed explanation\", \"codeSnippet\": { \"language\": \"Java\", \"code\": \"System.out.println('Hello, World!');\" },"
                        + "      \"image\": \"https://example.com/image.png\", \"video\": \"https://www.youtube.com/example\", \"references\": [{ \"title\": \"Reference\", \"url\": \"https://example.com/reference\", \"description\": \"Reference description\" }] } ] } ]",
                topic, subject
        );
    }

    // Build JSON request payload
    private String buildRequestPayload(String systemPrompt, String userInstruction) throws JsonProcessingException {
        Map<String, Object> requestPayload = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(Map.of("parts", List.of(Map.of("text", userInstruction))))
        );
        return objectMapper.writeValueAsString(requestPayload);
    }

    // Call Gemini or external API
    private String callApi(String jsonPayload) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(apiConfiguration.getApiUrl() + "?key=" + apiConfiguration.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    // Process API response
    private Map<String, Object> processApiResponse(String response, String subject) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(response);
        List<Map<String, Object>> extractedData = extractDataFromJson(rootNode);

        if (!extractedData.isEmpty()) {
            Map<String, Object> content = extractedData.get(0);

            // Save to MongoDB with collection name based on subject
            savePromptFromRequestBody(content, "jwefihwi");

            return content;
        }

        return Map.of("error", "No valid content found");
    }

    // Extract structured data from API response
    private List<Map<String, Object>> extractDataFromJson(JsonNode rootNode) throws JsonProcessingException {
        List<Map<String, Object>> extractedData = new ArrayList<>();
        JsonNode candidates = rootNode.path("candidates");

        for (JsonNode candidate : candidates) {
            JsonNode parts = candidate.path("content").path("parts");

            for (JsonNode part : parts) {
                String text = part.path("text").asText();
                if (text.contains("[") && text.contains("]")) {
                    text = text.substring(text.indexOf("["), text.lastIndexOf("]") + 1).trim();
                }

                List<Map<String, Object>> parsedData = objectMapper.readValue(text, new TypeReference<>() {});
                extractedData.addAll(parsedData);
            }
        }
        return extractedData;
    }

    // Save to MongoDB
    public String savePromptFromRequestBody(Map<String, Object> promptData, String collectionName) {
        try {
            Document document = new Document(promptData);
            mongoTemplate.insert(document, collectionName);
            return "Prompt data inserted from request body successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to insert prompt data from request body: " + e.getMessage();
        }
    }
}
