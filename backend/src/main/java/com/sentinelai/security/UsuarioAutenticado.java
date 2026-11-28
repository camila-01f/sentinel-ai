package com.sentinelai.security;

/** Datos del usuario que viajan dentro del token y quedan disponibles en cada petición. */
public record UsuarioAutenticado(Integer id, String correo, String nombre, String rol) { }
