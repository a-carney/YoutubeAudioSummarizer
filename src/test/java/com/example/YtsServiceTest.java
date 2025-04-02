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

import static com.example.TestConstants.*;
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


    @Test
    public void testIsValidYoutubeUrlOnValid() {
        assertTrue(service.isValidYoutubeUrl("https://www.youtube.com/watch?v=" + VID));
        assertTrue(service.isValidYoutubeUrl("https://youtu.be/" + VID));
        assertTrue(service.isValidYoutubeUrl("http://youtube.com/watch?v=" + VID));
    }
    @Test
    public void testIsValidYoutubeUrlOnInvalid() {
        assertTrue(service.isValidYoutubeUrl(null));
        assertTrue(service.isValidYoutubeUrl(""));
        assertTrue(service.isValidYoutubeUrl("https://www.google.com"));
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
        YtsDTO result = service.getSummaryAsDto(INVALID_URL);

        assertEquals(INVALID_URL, result.getUrl());
        assertTrue(result.getSummary().startsWith(INVALID_URL));
    }
}

