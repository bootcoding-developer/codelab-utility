package com.bootcoding.aws_ec2_notes_generator.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class NoteGeneratorService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public NoteGeneratorService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String generateNote(com.bootcoding.aws_ec2_notes_generator.app.NoteRequest noteRequest) {
        String prompt = buildPrompt(noteRequest);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {
            String response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("generativelanguage.googleapis.com")
                            .path("/v1beta/models/gemini-2.0-flash:generateContent")
                            .queryParam("key", geminiApiKey)
                            .build())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Extract the generated text content from response
            String extractedContent = extractResponseContent(response);

            // Save the content as Markdown file
            saveAsMarkdown(extractedContent);

            // Return the content as response
            return extractedContent;
        } catch (Exception e) {
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
    private void saveAsMarkdown(String content) {
        try {
            // Directory to save notes
            Path folderPath = Paths.get("/home/kumar/IdeaProjects/aws-ec2-notes-generator/generated-notes");
            Files.createDirectories(folderPath);

            // Create filename with full timestamp (date + time)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String filename = "aws_ec2_ques_ans_" + timestamp + ".md";
            Path filePath = folderPath.resolve(filename);

            // Write content to the file
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                writer.write(content);
            }

            System.out.println("Notes saved to Markdown file: " + filePath);
        } catch (IOException e) {
            System.out.println("Error saving Markdown file: " + e.getMessage());
        }
    }

    private String extractResponseContent(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private @NotNull String buildPrompt(com.bootcoding.aws_ec2_notes_generator.app.NoteRequest noteRequest) {
        StringBuilder prompt = new StringBuilder("Please generate AWS EC2 Questions & Answer based on the content below.");

        if (noteRequest.getTone() != null && !noteRequest.getTone().isEmpty()) {
            prompt.append(" Use a ").append(noteRequest.getTone()).append(" tone.");
        }

        prompt.append(" Here is the ASW EC2 Questions & Answers Note:\n").append(noteRequest.getContent());
        return prompt.toString();
    }
}
