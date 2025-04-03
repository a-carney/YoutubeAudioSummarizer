package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.TestConfig.FAKE_SUMMARY;
import static com.example.TestConfig.FAKE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class YtsDTOTest {


    @Test
    void testFromEntity() {
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
    void testToEntity() {
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
    void testFromEntity_NullInput() {
        assertNull(YtsDTO.fromEntity(null));
    }


}
