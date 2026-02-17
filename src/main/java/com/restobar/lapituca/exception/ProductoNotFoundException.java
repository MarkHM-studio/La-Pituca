package com.restobar.lapituca.exception;

public class ProductoNotFoundException extends RuntimeException{
    public ProductoNotFoundException(String productoNoEncontrado){
        super(productoNoEncontrado);
    }
}
