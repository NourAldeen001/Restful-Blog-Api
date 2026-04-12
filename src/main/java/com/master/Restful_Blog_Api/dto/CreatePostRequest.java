package com.master.Restful_Blog_Api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor /// Required for Jackson deserialization
@AllArgsConstructor
@Builder
public class CreatePostRequest {

    /// What we send for create post

    @NotBlank(message = "Title is required")
    @Size(min = 5, message = "Title must be 5-255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    private String content;

}
