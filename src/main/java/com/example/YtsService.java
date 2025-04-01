package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class YtsService {
    private static final Pattern YOUTUBE_URL_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([^&?\\s]+)");

    private final YtsRepository YTSRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.claude.api-key:your-api-key-here}")
    private String apiKey;

    @Value("${app.claude.api-endpoint:https://api.anthropic.com/v1/messages}")
    private String apiEndpoint;

    @Value("${app.claude.api-model:claude-3-opus-20240229}")
    private String apiModel;

    @Value("${app.claude.max-tokens:1000}")
    private int maxTokens;

    public YtsService(YtsRepository YTSRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.YTSRepository = YTSRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean isValidYoutubeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return YOUTUBE_URL_PATTERN.matcher(url).find();
    }

    public YtsDTO getSummaryAsDto(String videoUrl) {
        if (!isValidYoutubeUrl(videoUrl)) {
            YtsDTO dto = new YtsDTO(videoUrl);
            dto.setSummary("Invalid YouTube URL: " + videoUrl);
            return dto;
        }

        Optional<YtsVideo> existingVideo = YTSRepository.findByUrl(videoUrl);
        if (existingVideo.isPresent() && existingVideo.get().getSummary() != null) {
            return YtsDTO.fromEntity(existingVideo.get());
        }

        try {
            String transcript = getVideoTranscript(videoUrl);
            if (transcript == null || transcript.isEmpty()) {
                YtsDTO dto = new YtsDTO(videoUrl);
                dto.setSummary("Failed to obtain video transcript.");
                return dto;
            }

            String title = getVideoTitle(videoUrl);
            String summary = generateSummary(title, transcript);
            if (summary == null || summary.isEmpty()) {
                YtsDTO dto = new YtsDTO(videoUrl);
                dto.setSummary("Failed to generate summary.");
                return dto;
            }

            YtsVideo video = existingVideo.orElse(new YtsVideo(videoUrl));
            video.setSummary(summary);
            video = YTSRepository.save(video);

            return YtsDTO.fromEntity(video);
        } catch (Exception e) {
            YtsDTO dto = new YtsDTO(videoUrl);
            dto.setSummary("Error processing video: " + e.getMessage());
            return dto;
        }
    }

    public String getSummary(String videoUrl) {
        return getSummaryAsDto(videoUrl).getSummary();
    }

    private String getVideoTranscript(String videoUrl) {
        try {
            String downloadCommand = "yt-dlp -f 'bestaudio[ext=m4a]' -o 'temp_audio.m4a' " + videoUrl;
            int downloadResult = executeProcess(downloadCommand);
            if (downloadResult != 0) {
                return null;
            }

            String transcribeCommand = "whisper 'temp_audio.m4a' --model tiny --output_format txt --output_dir .";
            int transcribeResult = executeProcess(transcribeCommand);
            if (transcribeResult != 0) {
                return null;
            }

            String transcript = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get("temp_audio.txt")));

            executeProcess("rm temp_audio.m4a temp_audio.txt");
            return transcript;
        } catch (Exception e) {
            return null;
        }
    }

    private String getVideoTitle(String videoUrl) {
        try {
            String command = "yt-dlp --get-title " + videoUrl;
            Process process = Runtime.getRuntime().exec(command);

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            boolean completed = process.waitFor(30, TimeUnit.SECONDS);
            if (!completed) {
                process.destroy();
                return "Unknown Title";
            }

            return output.toString().trim();
        } catch (Exception e) {
            return "Unknown Title";
        }
    }

    private String generateSummary(String title, String transcript) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", apiModel);
            requestBody.put("max_tokens", maxTokens);

            String prompt = "[INSTRUCTIONS]: provide concise plain-text summary for video transcript\n\n" +
                    "Video Title: " + title + "\n\n" +
                    "Transcript:\n" + transcript + "\n\n" +
                    "summary should be as concise as possible and never more than 300 words in total";

            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt);

            requestBody.putArray("messages").add(message);
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            String responseBody = restTemplate.postForObject(apiEndpoint, request, String.class);
            JsonNode rootNode = objectMapper.readTree(responseBody);
            return rootNode.path("content").path(0).path("text").asText();
        } catch (Exception e) {
            return null;
        }
    }

    private int executeProcess(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        boolean completed = process.waitFor(300, TimeUnit.SECONDS);
        if (!completed) {
            process.destroy();
            throw new InterruptedException("Process execution timed out");
        }
        return process.exitValue();
    }
}
