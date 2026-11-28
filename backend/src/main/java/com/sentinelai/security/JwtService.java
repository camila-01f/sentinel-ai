package com.sentinelai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

/** Genera y valida los tokens JWT (firmados con HMAC-SHA). */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${sentinel.jwt.secret}") String secretoBase64,
                      @Value("${sentinel.jwt.expiracion-minutos:480}") long expiracionMinutos) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.expiracionMs = expiracionMinutos * 60_000L;
    }

    public String generar(Integer id, String correo, String nombre, String rol) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(correo)
                .claim("id", id)
                .claim("nombre", nombre)
                .claim("rol", rol)
                .issuedAt(new Date(ahora))
                .expiration(new Date(ahora + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el usuario si el token es válido y no ha expirado; vacío en cualquier otro caso. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            Number id = c.get("id", Number.class);
            String rol = c.get("rol", String.class);
            if (id == null || rol == null) {
                return Optional.empty();
            }
            return Optional.of(new UsuarioAutenticado(id.intValue(), c.getSubject(), c.get("nombre", String.class), rol));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }
}
