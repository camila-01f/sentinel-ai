package com.sentinelai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_recurso")
public class TipoRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    protected TipoRecurso() { }

    public TipoRecurso(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}
