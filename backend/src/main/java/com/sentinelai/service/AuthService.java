package com.sentinelai.service;

import com.sentinelai.dto.LoginRequest;
import com.sentinelai.dto.LoginResponse;
import com.sentinelai.dto.UsuarioDTO;
import com.sentinelai.exception.CredencialesInvalidasException;
import com.sentinelai.model.Usuario;
import com.sentinelai.repository.UsuarioRepository;
import com.sentinelai.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String MENSAJE_INVALIDO = "Correo o contraseña incorrectos";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByCorreoIgnoreCase(request.correo().trim())
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_INVALIDO));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException(MENSAJE_INVALIDO);
        }
        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException("El usuario está inactivo");
        }

        String rol = usuario.getRol().getNombre();
        String token = jwtService.generar(usuario.getId(), usuario.getCorreo(), usuario.getNombre(), rol);
        UsuarioDTO dto = new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), rol);
        return new LoginResponse(token, "Bearer", jwtService.getExpiracionSegundos(), rol, usuario.getNombre(), dto);
    }
}
