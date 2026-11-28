package com.sentinelai.controller;

import com.sentinelai.dto.LoginRequest;
import com.sentinelai.dto.LoginResponse;
import com.sentinelai.security.UsuarioAutenticado;
import com.sentinelai.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    /** POST /api/auth/login -> token JWT, rol y datos del usuario (401 si las credenciales no son válidas) */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    /** GET /api/auth/yo -> usuario dueño del token (sirve para comprobar que el token funciona) */
    @GetMapping("/yo")
    public UsuarioAutenticado yo(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario;
    }
}
