package com.example.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YtsUtil {
    private static final String PROTOCOL = "(?:https?://)?";
    private static final String DOMAIN = "(?:www\\.)?";
    private static final String PATH = "(?:youtube\\.com/watch\\?v=|youtu\\.be/)";
    private static final String VIDEO_ID = "([^&?\\s]+)";
    private static final Pattern YOUTUBE_URL_PATTERN = Pattern.compile(PROTOCOL + DOMAIN + PATH + VIDEO_ID);
    private static final int FIRST_GROUP = 1;
    private static final int TIMEOUT_MINUTES = 5;

    public static boolean isValidURL(String url) {
        return url != null && !url.isEmpty() && YOUTUBE_URL_PATTERN.matcher(url).find();
    }

    public static String extractVideoId(String url) {
        if (!isValidURL(url)) return null;

        Matcher matcher = YOUTUBE_URL_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(FIRST_GROUP) : null;
    }

    public static int execute(String command) {
        try {
            log.debug("Executing command: {}", command);
            Process process = Runtime.getRuntime().exec(command);
            boolean completed = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);

            if (!completed) {
                process.destroy();
                throw new YtsException("Execution timeout - command: " + command);
            }

            return process.exitValue();
        } catch (InterruptedException | IOException e) {
            throw new YtsException("Command execution failed: " + e.getMessage(), e);
        }
    }

    public static String capture(String command) {
        try {
            log.debug("Executing command with output capture: {}", command);
            Process process = Runtime.getRuntime().exec(command);
            String output = new String(process.getInputStream().readAllBytes());
            boolean completed = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);

            if (!completed) {
                process.destroy();
                throw new YtsException("Execution timeout - command: " + command);
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new YtsException("Command failed with exit code: " + exitCode);
            }

            return output.trim();
        } catch (IOException | InterruptedException e) {
            throw new YtsException("Command output capture failed: " + e.getMessage(), e);
        }
    }
}