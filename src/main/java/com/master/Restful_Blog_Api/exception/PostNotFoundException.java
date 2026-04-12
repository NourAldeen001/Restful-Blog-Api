package com.master.Restful_Blog_Api.exception;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long id) {
        super("Post not found with id: " + id);
    }

    public PostNotFoundException(String message) {
        super(message);
    }
}
