package com.pharaphara.uptoboxClientApi.entity;

import java.util.Map;

public class ResponseDTO {

    private String statusCode;
    private String message;
    private Map<String,Object> data;



    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String,Object> getData() {
        return data;
    }

    public void setData(Map<String,Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseDTO{" +
                "statusCode='" + statusCode + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
