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
public class CommentDTO {

    /// What (server) returns

    private Long id;

    private String content;

    private Long authorId;

    private String authorName;

    private Long postId;

    private String postTitle;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
