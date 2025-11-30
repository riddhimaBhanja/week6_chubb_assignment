package com.flightapp.exception;

public class PNRNotFoundException   extends RuntimeException {
    public PNRNotFoundException(String msg) {
        super(msg);
    }
}