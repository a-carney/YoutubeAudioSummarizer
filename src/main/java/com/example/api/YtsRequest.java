package com.example.api;

import com.example.common.YtsUtil;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a validated YouTube summary request
 */
@Getter
@Builder
public class YtsRequest {
    private final String url;
    private final String videoId;
    private final boolean valid;
    private final String validationError;

    /**
     * Factory method to create a request from a URL string
     *
     * @param url YouTube URL string
     * @return Validated YtsRequest
     */
    public static YtsRequest from(String url) {
        if (url == null || url.trim().isEmpty()) {
            return YtsRequest.builder()
                    .url(url)
                    .valid(false)
                    .validationError("URL is empty")
                    .build();
        }

        String videoId = YtsUtil.extractVideoId(url);
        if (videoId == null) {
            return YtsRequest.builder()
                    .url(url)
                    .valid(false)
                    .validationError("Invalid YouTube URL format")
                    .build();
        }

        return YtsRequest.builder()
                .url(url)
                .videoId(videoId)
                .valid(true)
                .build();
    }
}
