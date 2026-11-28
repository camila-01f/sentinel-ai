package com.sentinelai.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Formato único de los errores de la API (lo usan el manejador de errores y la seguridad). */
public final class RespuestaError {

    private RespuestaError() { }

    public static Map<String, Object> cuerpo(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fecha", LocalDateTime.now().withNano(0).toString());
        body.put("estado", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return body;
    }
}
