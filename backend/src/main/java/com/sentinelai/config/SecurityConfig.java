package com.sentinelai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelai.exception.RespuestaError;
import com.sentinelai.security.JwtAuthFilter;
import com.sentinelai.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Reglas de acceso de la API.
 *  - Público: login, estado de la API y catálogos de solo lectura.
 *  - Recursos: cualquier usuario autenticado los consulta; solo SUPERVISOR los crea, edita o elimina.
 *  - Incidentes: cualquier usuario autenticado los consulta, registra y edita; solo SUPERVISOR los elimina.
 *  - Todo lo demás (incluida la IA) requiere un token válido.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final ObjectMapper mapper;

    public SecurityConfig(JwtService jwtService, ObjectMapper mapper) {
        this.jwtService = jwtService;
        this.mapper = mapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                    escribir(res, HttpStatus.UNAUTHORIZED, "Debes iniciar sesión: falta el token o no es válido"))
                .accessDeniedHandler((req, res, ex) ->
                    escribir(res, HttpStatus.FORBIDDEN, "Tu rol no tiene permiso para esta operación")))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/api/estado", "/error").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/categorias", "/api/categorias/**",
                    "/api/prioridades", "/api/estados", "/api/tipos-recurso").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/recursos", "/api/recursos/**").authenticated()
                .requestMatchers("/api/recursos", "/api/recursos/**").hasRole("SUPERVISOR")
                .requestMatchers(HttpMethod.DELETE, "/api/incidentes/**").hasRole("SUPERVISOR")
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void escribir(HttpServletResponse res, HttpStatus status, String mensaje) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getOutputStream(), RespuestaError.cuerpo(status, mensaje));
    }
}
