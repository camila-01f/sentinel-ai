package com.sentinelai.exception;

/** 409: la operación choca con datos existentes (nombre repetido, registros relacionados...). */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
