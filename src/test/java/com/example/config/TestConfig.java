package com.example.config;

import com.example.service.ClaudeApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for test environment
 */
@Configuration
@Profile("test")
public class TestConfig {

    /**
     * Creates a mock ClaudeApiClient bean for testing
     * @param objectMapper Object mapper
     * @param restTemplate Rest template
     * @return Mocked ClaudeApiClient
     */
    @Bean
    @Primary
    public ClaudeApiClient claudeApiClient(ObjectMapper objectMapper, RestTemplate restTemplate) {
        ClaudeApiClient mockClient = Mockito.mock(ClaudeApiClient.class);

        // Configure default behavior for the mock
        Mockito.when(mockClient.generateSummary(Mockito.anyString(), Mockito.anyString()))
                .thenReturn("This is a mocked summary for testing purposes.");

        return mockClient;
    }
}