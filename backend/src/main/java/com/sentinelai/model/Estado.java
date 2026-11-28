package com.sentinelai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estados")
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String nombre;

    @Column(nullable = false, unique = true)
    private Short orden;

    protected Estado() { }

    public Estado(Integer id, String nombre, Short orden) {
        this.id = id;
        this.nombre = nombre;
        this.orden = orden;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public Short getOrden() { return orden; }
}
