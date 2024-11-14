package com.example.backend.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result<T> {
    public static final String CODE_SUCCESS = "200";
    public static final String CODE_AUTH_ERROR = "401";
    public static final String CODE_SYS_ERROR = "500";

    private String code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        return Result.<T>builder().code(CODE_SUCCESS).msg("请求成功").build();
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_SUCCESS, "请求成功", data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(CODE_SYS_ERROR, msg, null);
    }

    public static <T> Result<T> error(String code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error() {
        return new Result<>(CODE_SYS_ERROR, "系统错误", null);
    }
}