package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YtsDTO {
    private Long id;
    private String url;
    private String summary;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public YtsDTO(String url) {
        this.url = url;
        this.createdAt = LocalDateTime.now();
    }

    public static YtsDTO fromEntity(YtsVideo vid) {
        if (vid == null)    return null;

        return YtsDTO.builder()
                .id(vid.getId())
                .url(vid.getUrl())
                .summary(vid.getSummary())
                .createdAt(vid.getCreatedAt())
                .build();
    }

    public YtsVideo toEntity() {
        return YtsVideo.builder()
                .id(id)
                .url(url)
                .summary(summary)
                .createdAt(createdAt)
                .build();
    }
}
