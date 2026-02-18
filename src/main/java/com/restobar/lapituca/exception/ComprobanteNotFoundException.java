package com.restobar.lapituca.exception;

public class ComprobanteNotFoundException extends RuntimeException {
    public ComprobanteNotFoundException (String comprobanteNoEncontrado){
        super(comprobanteNoEncontrado);
    }
}
