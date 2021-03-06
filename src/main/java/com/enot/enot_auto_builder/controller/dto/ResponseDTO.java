package com.enot.enot_auto_builder.controller.dto;

public class ResponseDTO<T> {
    private boolean result;
    private T response;

    public ResponseDTO(boolean result, T response) {
        this.result = result;
        this.response = response;
    }

    public ResponseDTO(T response) {
        this.response = response;
        this.result = true;
    }

    public ResponseDTO() {
    }

    public boolean isResult() {
        return result;
    }

    public T getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "ResponseDTO{" +
                "result=" + result +
                ", response=" + response +
                '}';
    }
}
