package com.example.demo.exception;

public class AuthException extends RuntimeException{
    private final int code;
    public AuthException(String message) {
        this(401,message);
    }

    public AuthException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}
