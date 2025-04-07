package com.example.api;

import com.example.common.YtsUtil;
import com.example.model.YtsDTO;
import com.example.service.YtsService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling REST, CLI, and command-line requests for YouTube summaries
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class YtsController {
    private final YtsService service;

    /**
     * REST endpoint for getting a video summary
     *
     * @param url YouTube video URL
     * @return DTO with summary or error message
     */
    @GetMapping("/summary")
    public ResponseEntity<YtsDTO> getSummary(@RequestParam String url) {
        log.info("REST request received for URL: {}", url);

        if (!YtsUtil.isValidURL(url)) {
            log.warn("Invalid URL received: {}", url);
            return ResponseEntity.badRequest().body(new YtsDTO(url));
        }

        YtsDTO dto = service.getSummaryAsDto(url);
        return ResponseEntity.ok(dto);
    }

    /**
     * Shell/CLI component for interactive use
     */
    @ShellComponent
    @RequiredArgsConstructor
    public static class YtsCommands {
        private final YtsService service;

        @ShellMethod(value = "Summarize a YouTube Video", key = "summarize")
        public String summarize(@ShellOption String url) {
            return service.getSummary(url);
        }
    }

    /**
     * Command line runner for direct execution
     */
    @Component
    @RequiredArgsConstructor
    public static class YtRunner implements CommandLineRunner {
        private final YtsService service;
        private static final String USAGE = "Usage: java -jar ytsummarizer.jar <youtube-url>";
        private static final int EXIT_FAILURE = 1;
        private static final int EXIT_SUCCESS = 0;

        @Setter
        private String url;

        @Override
        public void run(String... args) throws Exception {
            // Skip if no arguments (Spring Boot is likely starting normally)
            if (args.length == 0) {
                return;
            }

            setup(args);
            printSummary();
            System.exit(EXIT_SUCCESS);
        }

        public void printSummary() {
            System.out.println(service.getSummary(url));
        }

        private void setup(String... args) {
            if (args.length != 1) {
                System.err.println(USAGE);
                System.exit(EXIT_FAILURE);
            }
            url = args[0];

            if (!YtsUtil.isValidURL(url)) {
                System.err.println("Invalid YouTube URL: " + url);
                System.err.println(USAGE);
                System.exit(EXIT_FAILURE);
            }
        }
    }
}