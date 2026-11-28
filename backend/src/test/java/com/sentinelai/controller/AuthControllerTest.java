package com.sentinelai.controller;

import com.sentinelai.config.SecurityConfig;
import com.sentinelai.dto.LoginResponse;
import com.sentinelai.dto.UsuarioDTO;
import com.sentinelai.exception.CredencialesInvalidasException;
import com.sentinelai.security.JwtService;
import com.sentinelai.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean AuthService service;
    @MockitoBean JwtService jwtService;

    @Test
    void loginCorrectoDevuelveToken() throws Exception {
        when(service.login(any())).thenReturn(new LoginResponse("token-123", "Bearer", 28800, "SUPERVISOR",
                "Supervisor Demo", new UsuarioDTO(1, "Supervisor Demo", "supervisor@sentinel.test", "SUPERVISOR")));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"correo\": \"supervisor@sentinel.test\", \"password\": \"Sentinel2026\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.token").value("token-123"))
           .andExpect(jsonPath("$.rol").value("SUPERVISOR"));
    }

    @Test
    void credencialesInvalidasResponden401() throws Exception {
        when(service.login(any())).thenThrow(new CredencialesInvalidasException("Correo o contraseña incorrectos"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"correo\": \"supervisor@sentinel.test\", \"password\": \"mala\"}"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    void correoVacioResponde400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"correo\": \"\", \"password\": \"x\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.campos.correo").exists());
    }
}
