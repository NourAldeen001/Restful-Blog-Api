package com.master.Restful_Blog_Api.exception;

public class PostWithoutAuthorException extends RuntimeException {

    public PostWithoutAuthorException() {
        super("Post must have author");
    }
}
