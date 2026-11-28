package com.sentinelai.dto;

/** Respuesta del login. La app Android lee token, rol, nombre y usuario. */
public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        String rol,
        String nombre,
        UsuarioDTO usuario
) { }
