package com.example;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class YtsDTO {
    private Long id;
    private String url;
    private String summary;
    private LocalDateTime createdAt;

    public YtsDTO(String url) {
        this.url = url;
        this.createdAt = LocalDateTime.now();
    }

    public static YtsDTO fromEntity(YtsVideo video) {
        if (video == null) return null;

        YtsDTO dto = new YtsDTO();
        dto.setId(video.getId());
        dto.setUrl(video.getUrl());
        dto.setSummary(video.getSummary());
        dto.setCreatedAt(video.getCreatedAt());
        return dto;
    }

    public YtsVideo toEntity() {
        YtsVideo entity = new YtsVideo();
        entity.setId(this.id);
        entity.setUrl(this.url);
        entity.setSummary(this.summary);
        entity.setCreatedAt(this.createdAt);
        return entity;
    }
}
