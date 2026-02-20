package com.restobar.lapituca.exception;

public class TipoEntregaNotFoundException extends RuntimeException{
    public TipoEntregaNotFoundException (String tipoEntregaNoEncontrado){
        super(tipoEntregaNoEncontrado);
    }
}
