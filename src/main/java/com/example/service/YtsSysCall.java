package com.example.service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum YtsSysCall {
    DOWNLOAD("yt-dlp -f 'bestaudio[ext=m4a]' -o 'temp_audio.m4a' %s"),
    TRANSCRIBE("whisper 'temp_audio.m4a' --model tiny --output_format txt --output_dir ."),
    CLEANUP("rm temp_audio.m4a temp_audio.txt"),
    GET_TITLE("yt-dlp --get-title %s");

    private final String commandTemplate;

    /**
     * Formats the command template with the provided arguments
     *
     * @param args Arguments to format into the command template
     * @return Formatted command string
     */
    public String getCommand(String... args) {
        return String.format(commandTemplate, (Object[]) args);
    }
}