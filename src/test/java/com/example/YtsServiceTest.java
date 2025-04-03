package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.TestConfig.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class YtsServiceTest {

    @Mock
    private YtsRepository repo;

    @Mock
    private RestTemplate template;

    @Mock
    private ObjectMapper mapper;

    private YtsService service;

    @BeforeEach
    void setUp() {
        service = new YtsService(repo, template, mapper);
    }


    @Test
    void testIsValidYoutubeUrlOnValid() {
        assertTrue(service.isValidYoutubeUrl("https://www.youtube.com/watch?v=" + VID));
        assertTrue(service.isValidYoutubeUrl("https://youtu.be/" + VID));
        assertTrue(service.isValidYoutubeUrl("http://youtube.com/watch?v=" + VID));
    }
    @Test
    void testIsValidYoutubeUrlOnInvalid() {
        assertFalse(service.isValidYoutubeUrl(null));
        assertFalse(service.isValidYoutubeUrl(""));
        assertFalse(service.isValidYoutubeUrl("https://www.google.com"));
    }



    @Test
    void testGetSummaryAsDtoOnExisting() {
        final String URL = "https://www.youtube.com/watch?v=" + VID;
        YtsVideo existing = new YtsVideo(URL);
        existing.setSummary(FAKE_SUMMARY);
        existing.setId(1L);
        existing.setCreatedAt(LocalDateTime.now());

        when(repo.findByUrl(URL)).thenReturn(Optional.of(existing));

        YtsDTO result = service.getSummaryAsDto(URL);
        assertEquals(FAKE_SUMMARY, result.getSummary());
        verify(repo).findByUrl(URL);
    }

    @Test
    void testGetSummaryAsDtoOnInvalidUrl() {
        YtsDTO result = service.getSummaryAsDto(INVALID_URL);

        assertEquals(INVALID_URL, result.getUrl());
        assertTrue(result.getSummary().contains(INVALID_URL));
    }

    @Test
    void testIsValidYoutubeUrlWithNullAndEmpty() {
        assertFalse(service.isValidYoutubeUrl(null));
        assertFalse(service.isValidYoutubeUrl(""));
    }
}

