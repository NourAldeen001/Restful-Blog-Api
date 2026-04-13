package com.master.Restful_Blog_Api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCommentRequest {

    @NotBlank(message = "Content is required")
    @Size(min = 5, message = "Content must be at least 5 characters")
    private String content;
}
