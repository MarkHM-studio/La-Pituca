package com.restobar.lapituca.exception;

public class MarcaNotFoundException extends RuntimeException {
    public MarcaNotFoundException(String marcaNoEncontrada) {
        super(marcaNoEncontrada);
    }
}
