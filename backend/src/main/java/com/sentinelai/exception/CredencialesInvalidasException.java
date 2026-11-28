package com.sentinelai.exception;

/** 401: correo o contraseña incorrectos, o usuario inactivo. */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
