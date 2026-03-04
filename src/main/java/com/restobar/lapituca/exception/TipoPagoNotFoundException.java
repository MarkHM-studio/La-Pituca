package com.restobar.lapituca.exception;

public class TipoPagoNotFoundException extends RuntimeException {
    public TipoPagoNotFoundException(String tipoPagoNoEncontrado) {
        super(tipoPagoNoEncontrado);
    }
}
