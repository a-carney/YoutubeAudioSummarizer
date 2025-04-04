package com.example.api.controller;

import com.example.api.dto.YtsDTO;
import com.example.core.service.YtsService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class YtsController {
    private final YtsProcessor processor;
    private final YtsService service;


    private static final String INVALID_URL = "Invalid Youtube Link - ";
    private static final String HEARTBEAT = "Server Running";
    @Setter @Getter
    private HttpStatus Status;

    @Setter @Getter
    private YtsRequest request;

    @Setter @Getter
    private YtsDTO dto;

    @Setter @Getter
    private String url;

    public YtsController(YtsService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public ResponseEntity<YtsDTO> getSummary(@RequestParam String url) {
        setUrl(url);
    }

    private void setRequest() {
        this.request = new YtsRequest(url);
    }

    private boolean isRequestValid() {
        return (request != null) && (request.isValid());
    }








}
