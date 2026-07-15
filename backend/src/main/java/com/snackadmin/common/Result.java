package com.snackadmin.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private String timestamp;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data, now());
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data, now());
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(400, message, null, now());
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, now());
    }

    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<>(code, message, data, now());
    }

    private static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
