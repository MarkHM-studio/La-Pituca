package com.restobar.lapituca.exception;

public class MesaNotFoundException extends RuntimeException{
    public MesaNotFoundException (String mesaNoEncontrada){
        super(mesaNoEncontrada);
    }
}
