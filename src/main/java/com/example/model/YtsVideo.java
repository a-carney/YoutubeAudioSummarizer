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
public class YtsVideo {
    private Long id;
    private String url;
    private String summary;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public YtsVideo(String url) {
        this.url = url;
        this.createdAt = LocalDateTime.now();
    }

    public boolean hasValidID() {
        return (id != null) && (id > 0);
    }
}

