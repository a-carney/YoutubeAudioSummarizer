package com.example.api.controller;

import com.example.api.dto.YtsDTO;
import com.example.core.service.YtsService;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class YtsProcessor {
    private static final String PROCESSING_ERROR = "Error processing video";
    private final YtsService service;

    @Getter
    private YtsRequest request;

    @Getter
    private YtsDTO dto;

    @Getter
    private String errorMessage;

    @Getter
    private HttpStatus errorstatus;

    @Getter
    private boolean processed;

    @Getter
    private boolean valid;

    public YtsProcessor(YtsService service) {
        this.service = service;
    }

}
