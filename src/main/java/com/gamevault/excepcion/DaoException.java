package com.gamevault.excepcion;

public class DaoException extends RuntimeException {
    public DaoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}