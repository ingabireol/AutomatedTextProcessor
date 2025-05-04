package com.dataflow.textprocessing.exception;

public class TextProcessingException extends RuntimeException {
    public TextProcessingException(String message) {
        super(message);
    }

    public TextProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
} 