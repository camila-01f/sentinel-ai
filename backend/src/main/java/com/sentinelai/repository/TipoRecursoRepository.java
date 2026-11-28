package com.sentinelai.repository;

import com.sentinelai.model.TipoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoRecursoRepository extends JpaRepository<TipoRecurso, Integer> {
    List<TipoRecurso> findAllByOrderByIdAsc();
}
