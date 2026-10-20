package com.skillovilla.readingmaterial.exception;

public class PublishConflictException extends ConcurrencyConflictException {

    public PublishConflictException(String message) {
        super(message);
    }
}
