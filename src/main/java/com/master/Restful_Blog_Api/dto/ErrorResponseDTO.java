package com.master.Restful_Blog_Api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {

    private int status;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    // In Case: Validation Errors
    private Map<String, String> errors;

}
