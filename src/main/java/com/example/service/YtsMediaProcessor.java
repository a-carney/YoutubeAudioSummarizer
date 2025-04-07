package com.example.service;

import com.example.common.YtsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Component responsible for processing YouTube video media
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YtsMediaProcessor {
    private static final int PROCESS_TIMEOUT_SECONDS = 30;
    private static final String TEMP_TRANSCRIPT_FILE = "temp_audio.txt";
    private static final String UNKNOWN_TITLE = "Unknown Title";

    /**
     * Extracts transcript from a YouTube video URL
     *
     * @param videoUrl YouTube video URL
     * @return Transcript text or null if extraction fails
     */
    public String extractTranscript(String videoUrl) {
        try {
            // Download audio
            log.info("Downloading audio from: {}", videoUrl);
            int downloadResult = executeCommand(YtsSysCall.DOWNLOAD.getCommand(videoUrl));
            if (downloadResult != 0) {
                log.error("Failed to download audio: exit code {}", downloadResult);
                return null;
            }

            // Transcribe audio
            log.info("Transcribing audio");
            int transcribeResult = executeCommand(YtsSysCall.TRANSCRIBE.getCommand());
            if (transcribeResult != 0) {
                log.error("Failed to transcribe audio: exit code {}", transcribeResult);
                return null;
            }

            // Read transcript
            String transcript = Files.readString(Path.of(TEMP_TRANSCRIPT_FILE));
            log.info("Transcript extracted successfully ({} characters)", transcript.length());

            // Cleanup
            executeCommand(YtsSysCall.CLEANUP.getCommand());
            return transcript;
        } catch (Exception e) {
            log.error("Error extracting transcript: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Retrieves the title of a YouTube video
     *
     * @param videoUrl YouTube video URL
     * @return Video title or "Unknown Title" if retrieval fails
     */
    public String getVideoTitle(String videoUrl) {
        try {
            log.info("Retrieving title for video: {}", videoUrl);
            String command = YtsSysCall.GET_TITLE.getCommand(videoUrl);
            Process process = Runtime.getRuntime().exec(command);

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            boolean completed = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                process.destroy();
                log.warn("Timeout while getting video title");
                return UNKNOWN_TITLE;
            }

            String title = output.toString().trim();
            log.info("Retrieved title: {}", title);
            return title.isEmpty() ? UNKNOWN_TITLE : title;
        } catch (Exception e) {
            log.error("Error getting video title: {}", e.getMessage(), e);
            return UNKNOWN_TITLE;
        }
    }

    /**
     * Executes a system command
     *
     * @param command The command to execute
     * @return Exit code of the process
     * @throws Exception If the process fails or times out
     */
    private int executeCommand(String command) throws Exception {
        log.debug("Executing command: {}", command);
        Process process = Runtime.getRuntime().exec(command);

        boolean completed = process.waitFor(5, TimeUnit.MINUTES);
        if (!completed) {
            process.destroy();
            throw new YtsException("Process execution timed out: " + command);
        }

        return process.exitValue();
    }
}


