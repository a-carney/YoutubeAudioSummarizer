package com.example.service;

import com.example.common.YtsErrorType;
import com.example.common.YtsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Client for interacting with Claude API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeApiClient {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${app.claude.api-key}")
    private String apiKey;

    @Value("${app.claude.api-endpoint:https://api.anthropic.com/v1/messages}")
    private String apiEndpoint;

    @Value("${app.claude.api-model:claude-3-opus-20240229}")
    private String apiModel;

    @Value("${app.claude.max-tokens:1000}")
    private int maxTokens;

    /**
     * Generates a summary of a video transcript using Claude API
     *
     * @param title      Video title
     * @param transcript Video transcript
     * @return Generated summary text
     * @throws YtsException If the API call fails
     */
    public String generateSummary(String title, String transcript) {
        try {
            log.info("Generating summary for video: {}", title);
            HttpHeaders headers = createHeaders();
            ObjectNode requestBody = createRequestBody(title, transcript);

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            String responseBody = restTemplate.postForObject(apiEndpoint, request, String.class);

            String summary = extractSummaryFromResponse(responseBody);
            log.info("Summary generated successfully ({} characters)", summary.length());
            return summary;
        } catch (RestClientException e) {
            log.error("API request to Claude failed: {}", e.getMessage(), e);
            throw YtsException.get("Claude API call failed: " + e.getMessage(), YtsErrorType.PROCESSING);
        } catch (Exception e) {
            log.error("Error generating summary: {}", e.getMessage(), e);
            throw YtsException.get("Summary generation failed: " + e.getMessage(), YtsErrorType.PROCESSING);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        return headers;
    }

    private ObjectNode createRequestBody(String title, String transcript) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", apiModel);
        requestBody.put("max_tokens", maxTokens);

        String prompt = createPrompt(title, transcript);

        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);

        requestBody.putArray("messages").add(message);
        return requestBody;
    }

    private String createPrompt(String title, String transcript) {
        return """
                [INSTRUCTIONS]: provide concise plain-text summary for video transcript
                
                Video Title: %s
                
                Transcript:
                %s
                
                summary should be as concise as possible and never more than 300 words in total
                """.formatted(title, transcript);
    }

    private String extractSummaryFromResponse(String responseBody) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        return rootNode.path("content").path(0).path("text").asText();
    }
}
