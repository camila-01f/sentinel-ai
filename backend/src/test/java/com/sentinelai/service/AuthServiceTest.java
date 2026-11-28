package com.sentinelai.service;

import com.sentinelai.dto.LoginRequest;
import com.sentinelai.dto.LoginResponse;
import com.sentinelai.exception.CredencialesInvalidasException;
import com.sentinelai.model.Rol;
import com.sentinelai.model.Usuario;
import com.sentinelai.repository.UsuarioRepository;
import com.sentinelai.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarios;
    @Mock JwtService jwtService;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private AuthService service;
    private Usuario supervisor;

    @BeforeEach
    void preparar() {
        service = new AuthService(usuarios, encoder, jwtService);
        supervisor = new Usuario(1, "Supervisor Demo", "supervisor@sentinel.test",
                encoder.encode("Sentinel2026"), new Rol(1, "SUPERVISOR"), true);
    }

    @Test
    void loginCorrectoDevuelveTokenYRol() {
        when(usuarios.findByCorreoIgnoreCase("supervisor@sentinel.test")).thenReturn(Optional.of(supervisor));
        when(jwtService.generar(1, "supervisor@sentinel.test", "Supervisor Demo", "SUPERVISOR")).thenReturn("token-123");
        when(jwtService.getExpiracionSegundos()).thenReturn(28800L);

        LoginResponse r = service.login(new LoginRequest("supervisor@sentinel.test", "Sentinel2026"));

        assertThat(r.token()).isEqualTo("token-123");
        assertThat(r.rol()).isEqualTo("SUPERVISOR");
        assertThat(r.usuario().correo()).isEqualTo("supervisor@sentinel.test");
    }

    @Test
    void contrasenaIncorrectaLanza401() {
        when(usuarios.findByCorreoIgnoreCase("supervisor@sentinel.test")).thenReturn(Optional.of(supervisor));

        assertThatThrownBy(() -> service.login(new LoginRequest("supervisor@sentinel.test", "otra")))
                .isInstanceOf(CredencialesInvalidasException.class);
        verify(jwtService, never()).generar(any(), anyString(), anyString(), anyString());
    }

    @Test
    void correoInexistenteLanza401() {
        when(usuarios.findByCorreoIgnoreCase("nadie@sentinel.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("nadie@sentinel.test", "Sentinel2026")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void usuarioInactivoNoPuedeEntrar() {
        Usuario inactivo = new Usuario(3, "Inactivo", "inactivo@sentinel.test",
                encoder.encode("Sentinel2026"), new Rol(2, "AGENTE"), false);
        when(usuarios.findByCorreoIgnoreCase("inactivo@sentinel.test")).thenReturn(Optional.of(inactivo));

        assertThatThrownBy(() -> service.login(new LoginRequest("inactivo@sentinel.test", "Sentinel2026")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessageContaining("inactivo");
    }
}
