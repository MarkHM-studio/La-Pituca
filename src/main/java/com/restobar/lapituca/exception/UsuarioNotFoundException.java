package com.restobar.lapituca.exception;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(String usuarioNoEncontrado) {
        super(usuarioNoEncontrado);
    }
}
