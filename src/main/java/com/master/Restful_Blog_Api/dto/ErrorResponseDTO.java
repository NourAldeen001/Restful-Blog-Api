package com.master.Restful_Blog_Api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponseDTO {

    private int status;
    private String message;
    private String path;
    private LocalDateTime timestamp;

}
