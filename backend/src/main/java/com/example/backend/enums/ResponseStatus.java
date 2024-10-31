package com.example.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseStatus {
    SUCCESS("000000", "success"),
    SYSTEM_ERROR("999999", "internal server error"),
    ;
    private String code;
    private String message;
}
