package com.restobar.lapituca.exception;

public class PedidoNotFoundException extends RuntimeException {
    public PedidoNotFoundException(String pedidoNoEncontrado){
        super(pedidoNoEncontrado);
    }
}
