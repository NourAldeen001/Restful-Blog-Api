package com.master.Restful_Blog_Api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    ///  200 OK with data ->> Update
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    /// 201 CREATED with data ->> Create
    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message(message)
                .data(data)
                .build();
    }

    /// 200 OK without data ->> Delete
    public static <T> ApiResponse<T> deleted(String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .build();
    }

}
