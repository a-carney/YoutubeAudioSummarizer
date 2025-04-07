package com.example;

import com.example.api.YtsController;
import com.example.model.YtsDTO;
import com.example.service.YtsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class YtsControllerTest {

    @Mock
    private YtsService service;

    @InjectMocks
    private YtsController controller;

    private final String validUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
    private final String invalidUrl = "https://example.com/not-youtube";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSummary_withValidUrl_returnsOkResponse() {
        // Arrange
        YtsDTO dto = new YtsDTO(validUrl);
        dto.setSummary("Test summary");
        when(service.getSummaryAsDto(validUrl)).thenReturn(dto);

        // Act
        ResponseEntity<YtsDTO> response = controller.getSummary(validUrl);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(service).getSummaryAsDto(validUrl);
    }

    @Test
    void getSummary_withInvalidUrl_returnsBadRequest() {
        // Act
        ResponseEntity<YtsDTO> response = controller.getSummary(invalidUrl);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(invalidUrl, response.getBody().getUrl());
    }
}
