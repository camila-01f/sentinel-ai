package com.sentinelai.security;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRETO = "c2VudGluZWwtYWktY2xhdmUtZGUtZGVzYXJyb2xsby1zb2xvLXBhcmEtcHJ1ZWJhcy0yMDI2";

    @Test
    void generaYValidaUnToken() {
        JwtService jwt = new JwtService(SECRETO, 60);
        String token = jwt.generar(1, "supervisor@sentinel.test", "Supervisor Demo", "SUPERVISOR");

        Optional<UsuarioAutenticado> usuario = jwt.validar(token);

        assertThat(usuario).isPresent();
        assertThat(usuario.get().id()).isEqualTo(1);
        assertThat(usuario.get().correo()).isEqualTo("supervisor@sentinel.test");
        assertThat(usuario.get().rol()).isEqualTo("SUPERVISOR");
    }

    @Test
    void rechazaUnTokenAlterado() {
        JwtService jwt = new JwtService(SECRETO, 60);
        String token = jwt.generar(2, "agente@sentinel.test", "Agente Demo", "AGENTE");
        String alterado = token.substring(0, token.length() - 3) + "abc";

        assertThat(jwt.validar(alterado)).isEmpty();
    }

    @Test
    void rechazaUnTokenExpirado() {
        JwtService jwt = new JwtService(SECRETO, -1);
        String token = jwt.generar(2, "agente@sentinel.test", "Agente Demo", "AGENTE");

        assertThat(jwt.validar(token)).isEmpty();
    }

    @Test
    void rechazaTextoQueNoEsToken() {
        JwtService jwt = new JwtService(SECRETO, 60);
        assertThat(jwt.validar("esto-no-es-un-token")).isEmpty();
    }
}
