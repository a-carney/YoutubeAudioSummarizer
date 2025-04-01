package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.example.TestConstants.FAKE_SUMMARY;
import static com.example.TestConstants.FAKE_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class YtsDTOTest {


    @Test
    public void testFromEntity() {
        YtsVideo video = new YtsVideo(FAKE_URL);
        video.setId(1L);
        video.setSummary(FAKE_SUMMARY);

        YtsDTO dto = YtsDTO.fromEntity(video);

        assertEquals(video.getId(), dto.getId());
        assertEquals(video.getUrl(), dto.getUrl());
        assertEquals(video.getSummary(), dto.getSummary());
        assertEquals(video.getCreatedAt(), dto.getCreatedAt());
    }

    @Test
    public void testToEntity() {
        YtsDTO dto = new YtsDTO(FAKE_URL);
        dto.setId(1L);
        dto.setSummary(FAKE_SUMMARY);

        YtsVideo video = dto.toEntity();

        assertEquals(dto.getId(), video.getId());
        assertEquals(dto.getUrl(), video.getUrl());
        assertEquals(dto.getSummary(), video.getSummary());
        assertEquals(dto.getCreatedAt(), video.getCreatedAt());
    }

    @Test
    public void testFromEntity_NullInput() {
        assertNull(YtsDTO.fromEntity(null));
    }


}
