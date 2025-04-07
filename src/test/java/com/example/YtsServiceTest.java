package com.example;

import com.example.data.YtsRepository;
import com.example.model.YtsDTO;
import com.example.model.YtsVideo;
import com.example.service.ClaudeApiClient;
import com.example.service.YtsMediaProcessor;
import com.example.service.YtsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class YtsServiceTest {

    @Mock
    private YtsRepository repository;

    @Mock
    private YtsMediaProcessor mediaProcessor;

    @Mock
    private ClaudeApiClient claudeApiClient;

    @InjectMocks
    private YtsService service;

    private final String validUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
    private final String invalidUrl = "https://example.com/not-youtube";
    private final String testTranscript = "Test transcript";
    private final String testTitle = "Test Video Title";
    private final String testSummary = "Test summary";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void isValidYoutubeUrl_withValidUrl_returnsTrue() {
        assertTrue(service.isValidYoutubeUrl(validUrl));
    }

    @Test
    void isValidYoutubeUrl_withInvalidUrl_returnsFalse() {
        assertFalse(service.isValidYoutubeUrl(invalidUrl));
    }

    @Test
    void getSummaryAsDto_withInvalidUrl_returnsErrorDto() {
        // Act
        YtsDTO result = service.getSummaryAsDto(invalidUrl);

        // Assert
        assertNotNull(result);
        assertEquals(invalidUrl, result.getUrl());
        assertTrue(result.getSummary().startsWith("Error:"));
    }

    @Test
    void getSummaryAsDto_withCachedVideo_returnsCachedSummary() {
        // Arrange
        YtsVideo cachedVideo = new YtsVideo(validUrl);
        cachedVideo.setSummary(testSummary);
        when(repository.findByUrl(validUrl)).thenReturn(Optional.of(cachedVideo));

        // Act
        YtsDTO result = service.getSummaryAsDto(validUrl);

        // Assert
        assertEquals(testSummary, result.getSummary());
        verifyNoInteractions(mediaProcessor);
        verifyNoInteractions(claudeApiClient);
    }

    @Test
    void getSummaryAsDto_withNewVideo_generatesAndSavesSummary() {
        // Arrange
        when(repository.findByUrl(validUrl)).thenReturn(Optional.empty());
        when(mediaProcessor.extractTranscript(validUrl)).thenReturn(testTranscript);
        when(mediaProcessor.getVideoTitle(validUrl)).thenReturn(testTitle);
        when(claudeApiClient.generateSummary(testTitle, testTranscript)).thenReturn(testSummary);
        when(repository.save(any(YtsVideo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        YtsDTO result = service.getSummaryAsDto(validUrl);

        // Assert
        assertEquals(testSummary, result.getSummary());
        verify(mediaProcessor).extractTranscript(validUrl);
        verify(mediaProcessor).getVideoTitle(validUrl);
        verify(claudeApiClient).generateSummary(testTitle, testTranscript);
        verify(repository).save(any(YtsVideo.class));
    }

    @Test
    void getSummaryAsDto_whenTranscriptExtrationFails_returnsErrorDto() {
        // Arrange
        when(repository.findByUrl(validUrl)).thenReturn(Optional.empty());
        when(mediaProcessor.extractTranscript(validUrl)).thenReturn(null);

        // Act
        YtsDTO result = service.getSummaryAsDto(validUrl);

        // Assert
        assertTrue(result.getSummary().startsWith("Error:"));
        verify(mediaProcessor).extractTranscript(validUrl);
        verifyNoInteractions(claudeApiClient);
    }
}
