package com.sentinelai.exception;

/** 400: la solicitud hace referencia a datos que no existen o no son válidos. */
public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
