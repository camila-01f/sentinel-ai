package com.sentinelai.service;

import com.sentinelai.dto.CategoriaDTO;
import com.sentinelai.exception.RecursoNoEncontradoException;
import com.sentinelai.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaDTO> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(CategoriaDTO::from).toList();
    }

    public CategoriaDTO obtener(Integer id) {
        return repository.findById(id)
                .map(CategoriaDTO::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría con id " + id));
    }
}
