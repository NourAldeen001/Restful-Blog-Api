package com.master.Restful_Blog_Api.exception;

public class CommentNotBelongsToPostException extends RuntimeException {

    public CommentNotBelongsToPostException(String message) {
        super(message);
    }

    public CommentNotBelongsToPostException(Long commentId, Long postId) {
        super("Comment [%s] does not belong to post [%s]".formatted(commentId, postId));
    }
}
