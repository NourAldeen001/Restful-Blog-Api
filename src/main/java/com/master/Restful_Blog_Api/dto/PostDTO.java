package com.master.Restful_Blog_Api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {

    /// What (server) returns
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
