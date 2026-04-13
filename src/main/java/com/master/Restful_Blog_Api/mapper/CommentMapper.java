package com.master.Restful_Blog_Api.mapper;

import com.master.Restful_Blog_Api.dto.CommentDTO;
import com.master.Restful_Blog_Api.dto.CreateCommentRequest;
import com.master.Restful_Blog_Api.dto.UpdateCommentRequest;
import com.master.Restful_Blog_Api.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDTO toCommentDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getUsername() : null)
                .postId(comment.getPost() != null ? comment.getPost().getId() : null)
                .postTitle(comment.getPost() != null ? comment.getPost().getTitle() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    public Comment toEntity(CreateCommentRequest createCommentRequest) {
        return Comment.builder()
                .content(createCommentRequest.getContent())
                .build();
    }

    public Comment toEntity(UpdateCommentRequest updateCommentRequest) {
        return Comment.builder()
                .content(updateCommentRequest.getContent())
                .build();
    }
}
