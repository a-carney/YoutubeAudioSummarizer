package com.example;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class YtsCommands {

    private final YtsService ytsService;

    public YtsCommands(YtsService ytsService) {
        this.ytsService = ytsService;
    }

    @ShellMethod(value = "Summarize a YouTube video", key = "summarize")
    public String summarize(@ShellOption String url) {
        return ytsService.getSummary(url);
    }
}
