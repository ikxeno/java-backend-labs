package io.github.ikxeno.service;

public final class AirplaneNotFoundException extends RuntimeException {
    public AirplaneNotFoundException(String message) {
        super(message);
    }
}
