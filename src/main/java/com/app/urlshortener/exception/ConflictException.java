package com.app.urlshortener.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
