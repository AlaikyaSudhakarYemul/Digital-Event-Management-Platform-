package com.wipro.admin.exception;

public class SpeakerNotFoundException extends RuntimeException {
    public SpeakerNotFoundException(String message) {
        super(message);
    }
}
