package com.sentinelai.repository;

import com.sentinelai.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
}
