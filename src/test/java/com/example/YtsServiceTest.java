package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.TestConstants.FAKE_SUMMARY;
import static com.example.TestConstants.VID;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class YtsServiceTest {

    @Mock
    private YtsRepository repo;

    @Mock
    private RestTemplate template;

    @Mock
    private ObjectMapper mapper;

    private YtsService service;

    @Before
    public void setUp() {
        service = new YtsService(repo, template, mapper);
    }


//
    @Test
    public void testIsValidYoutubeUrlOnValid() {
        assertTrue(service.isValidYoutubeUrl("https://www.youtube.com/watch?v=" + VID));
        assertTrue(service.isValidYoutubeUrl("https://youtu.be/" + VID));
        assertTrue(service.isValidYoutubeUrl("http://youtube.com/watch?v=" + VID));
    }
    @Test
    public void testIsValidYoutubeUrlOnInvalid() {
        assertEquals(true, service.isValidYoutubeUrl(null));
        assertEquals(true, service.isValidYoutubeUrl(""));
        assertEquals(true, service.isValidYoutubeUrl("https://www.google.com"));

    }



    @Test
    public void testGetSummaryAsDtoOnExisting() {
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
    public void testGetSummaryAsDto_InvalidUrl() {
        String invalidUrl = "invalid-url";

        YtsDTO result = service.getSummaryAsDto(invalidUrl);

        assertEquals(invalidUrl, result.getUrl());
        assertTrue(result.getSummary().startsWith("Invalid YouTube URL"));
    }
}

