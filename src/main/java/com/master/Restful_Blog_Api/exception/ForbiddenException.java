package com.master.Restful_Blog_Api.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException() {
        super("You don't have permission to perform this action");
    }
}
