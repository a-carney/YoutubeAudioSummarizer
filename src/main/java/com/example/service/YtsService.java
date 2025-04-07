package com.example.service;

import com.example.common.YtsException;
import com.example.common.YtsUtil;
import com.example.data.YtsRepository;
import com.example.model.YtsDTO;
import com.example.model.YtsVideo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for YouTube video summarization
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YtsService {
    private final YtsRepository repository;
    private final YtsMediaProcessor mediaProcessor;
    private final ClaudeApiClient claudeApiClient;

    /**
     * Validates if a URL is a valid YouTube URL
     *
     * @param url URL to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidYoutubeUrl(String url) {
        return YtsUtil.isValidURL(url);
    }

    /**
     * Gets a summary for a YouTube video
     *
     * @param url YouTube video URL
     * @return DTO containing the summary
     */
    public YtsDTO getSummaryAsDto(String url) {
        log.info("Processing summary request for URL: {}", url);

        // Validate URL
        if (!isValidYoutubeUrl(url)) {
            log.warn("Invalid YouTube URL: {}", url);
            return createErrorDto(url, "Invalid YouTube URL format");
        }

        // Check cache
        YtsVideo existingVideo = repository.findByUrl(url).orElse(null);
        if (existingVideo != null && existingVideo.getSummary() != null) {
            log.info("Returning cached summary for URL: {}", url);
            return YtsDTO.fromEntity(existingVideo);
        }

        try {
            // Get transcript
            String transcript = mediaProcessor.extractTranscript(url);
            if (transcript == null || transcript.isEmpty()) {
                return createErrorDto(url, "Failed to obtain video transcript");
            }

            // Get title
            String title = mediaProcessor.getVideoTitle(url);

            // Generate summary
            String summary = claudeApiClient.generateSummary(title, transcript);
            if (summary == null || summary.isEmpty()) {
                return createErrorDto(url, "Failed to generate summary");
            }

            // Save and return
            return saveAndReturnSummary(url, summary, existingVideo);
        } catch (YtsException e) {
            log.error("Error processing video: {}", e.getMessage(), e);
            return createErrorDto(url, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return createErrorDto(url, "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Gets a summary text for a YouTube video
     *
     * @param url YouTube video URL
     * @return Summary text
     */
    public String getSummary(String url) {
        return getSummaryAsDto(url).getSummary();
    }

    // Private helper methods
    private YtsDTO createErrorDto(String url, String errorMessage) {
        YtsDTO dto = new YtsDTO(url);
        dto.setSummary("Error: " + errorMessage);
        return dto;
    }

    private YtsDTO saveAndReturnSummary(String url, String summary, YtsVideo existingVideo) {
        YtsVideo video = existingVideo != null ? existingVideo : new YtsVideo(url);
        video.setSummary(summary);
        repository.save(video);
        log.info("Summary saved for URL: {}", url);
        return YtsDTO.fromEntity(video);
    }
}
