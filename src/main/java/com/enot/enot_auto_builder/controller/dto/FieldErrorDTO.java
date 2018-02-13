package com.enot.enot_auto_builder.controller.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class FieldErrorDTO {
    @NonNull
    private String field;
    @NonNull
    private String message;
}
