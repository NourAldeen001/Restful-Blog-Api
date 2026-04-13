package com.master.Restful_Blog_Api.service;

import com.master.Restful_Blog_Api.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {

    // Global Comment Operations
    List<Comment> getAllComments();
    Comment getCommentById(Long id);
    Comment addComment(Comment comment);
    Comment updateComment(Long id, Comment newComment);
    void deleteComment(Long id);

    // Post-specific Operations
    List<Comment> getCommentsByPostId(Long postId);
    Page<Comment> getCommentsByPostId(Long postId, Pageable pageable);

}
