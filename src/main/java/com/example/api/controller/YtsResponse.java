package com.example.api.controller;

import com.example.api.dto.YtsDTO;
import org.springframework.http.HttpStatus;

public class YtsResponse {
    private YtsDTO dto;
    private String msg;
    private HttpStatus status;
    private int statusCode;
    private boolean success;
    private long timestamp;

    public YtsResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public static YtsResponse success(YtsDTO dto) {
        return new YtsResponse()
                .setDto(dto)
                .setSuccess(true)
                .setStatus(HttpStatus.OK);
    }

    public static YtsResponse error(YtsDTO dto, String msg, HttpStatus status) {
        return new YtsResponse()
                .setDto(dto)
                .setMsg(msg)
                .setSuccess(false)
                .setStatus(status);
    }


    public YtsResponse setDto(YtsDTO dto) {
        this.dto = dto;
        return this;
    }

    public YtsResponse setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public YtsResponse setStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public YtsResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    private void setStatusCode() {
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        statusCode = status.value();
    }

    public int getStatusCode() {
        return statusCode;
    }


}

