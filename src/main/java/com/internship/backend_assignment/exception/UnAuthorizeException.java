package com.internship.backend_assignment.exception;

public class UnAuthorizeException extends RuntimeException{
    public UnAuthorizeException(String message ){
        super(message);
    }
}
