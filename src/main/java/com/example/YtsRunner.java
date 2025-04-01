package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class YtsRunner implements CommandLineRunner {
    private final YtsService service;

    public YtsRunner(YtsService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar video-summary.jar <youtube-url>");
            System.exit(1);
        }

        String url = args[0];
        String summary = service.getSummary(url);
        System.out.println(summary);
        System.exit(0);
    }
}