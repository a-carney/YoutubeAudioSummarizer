package com.example;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class YtsVideo {
    private Long id;
    private String url;
    private String summary;
    private LocalDateTime createdAt;

    public YtsVideo(String url) {
        this.url = url;
        this.createdAt = LocalDateTime.now();
    }
}