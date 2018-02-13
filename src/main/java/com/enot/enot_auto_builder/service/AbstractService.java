package com.enot.enot_auto_builder.service;

import com.enot.enot_auto_builder.controller.dto.ResponseDTO;

public abstract class AbstractService {
    ResponseDTO<String> fail(String message) {
        return new ResponseDTO<>(false, message);
    }

    ResponseDTO<String> ok() {
        return ok("ok");
    }

    <T> ResponseDTO<T> ok(T message) {
        return new ResponseDTO<>(message);
    }
}
