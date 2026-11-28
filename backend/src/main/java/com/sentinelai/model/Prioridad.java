package com.sentinelai.model;

import jakarta.persistence.*;

/** Catálogo de prioridades. La columna color no se mapea porque el backend no la usa. */
@Entity
@Table(name = "prioridades")
public class Prioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String nombre;

    @Column(nullable = false, unique = true)
    private Short nivel;

    protected Prioridad() { }

    public Prioridad(Integer id, String nombre, Short nivel) {
        this.id = id;
        this.nombre = nombre;
        this.nivel = nivel;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public Short getNivel() { return nivel; }
}
