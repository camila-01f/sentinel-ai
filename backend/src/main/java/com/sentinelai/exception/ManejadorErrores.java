package com.sentinelai.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Respuestas de error uniformes en JSON para toda la API. */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Map<String, Object>> solicitudInvalida(SolicitudInvalidaException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Map<String, Object>> conflicto(ConflictoException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> credenciales(CredencialesInvalidasException e) {
        return respuesta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    /** Errores de @Valid: devuelve el mensaje de cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> body = RespuestaError.cuerpo(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud");
        body.put("campos", campos);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> cuerpoIlegible(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "Parámetro inválido: " + e.getName());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridad(DataIntegrityViolationException e) {
        log.warn("Violación de integridad: {}", e.getMostSpecificCause().getMessage());
        return respuesta(HttpStatus.CONFLICT, "La operación viola una restricción de la base de datos");
    }

    /** Errores lanzados con ResponseStatusException (por ejemplo, en los servicios de IA). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> conEstado(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String mensaje = e.getReason() != null ? e.getReason() : status.getReasonPhrase();
        return respuesta(status, mensaje);
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(RespuestaError.cuerpo(status, mensaje));
    }
}
