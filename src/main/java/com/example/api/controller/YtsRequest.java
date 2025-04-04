package com.example.api.controller;

import org.springframework.http.HttpStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YtsRequest {
    private static final Pattern YOUTUBE_URL_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([^&?\\s]+)");
    private static final String URL_EMPTY = "URL is empty";
    private static final String URL_INVALID = "Invalid Youtube Link - ";
    private static final String HEARTBEAT = "Server Running";

    private final String url;
    private final String videoId;
    private final boolean valid;
    private final String validationError;

    public YtsRequest(String url, String videoId, boolean valid, String validationError) {
        this.url = url;
        this.videoId = videoId;
        this.valid = valid;
        this.validationError = validationError;
    }


    public static YtsRequest from(String url) {
        if (url == null || url.isEmpty()) {
            return new YtsRequest(url, null, false, URL_EMPTY);
        }

        String vid = extractVideoId(url);
        if (vid == null) {
            return new YtsRequest(url, null, false, URL_INVALID + url);
        }

        return new YtsRequest(url, vid, true, null);
    }

    private static String extractVideoId(String url) {
        Matcher m = YOUTUBE_URL_PATTERN.matcher(url);
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    public String getUrl() {
        return url;
    }

    public String getVideoId() {
        return videoId;
    }

    public boolean isValid() {
        return valid;
    }

    public String getValidationError() {
        return validationError;
    }

    public YtsResponse createErrorResponse() {
        return YtsResponse.error(
                null,
                valid ? null : validationError,
                valid ? null : HttpStatus.BAD_REQUEST
        );
    }

    public boolean isYoutubeUrl() {
        return valid && videoId != null;
    }

    private static String getHeartbeat() {
        return HEARTBEAT;
    }
}
