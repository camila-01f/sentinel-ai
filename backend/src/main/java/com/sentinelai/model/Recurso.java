package com.sentinelai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recursos")
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_recurso_id", nullable = false)
    private TipoRecurso tipoRecurso;

    @Column(nullable = false)
    private boolean disponible = true;

    public Recurso() { }

    public Recurso(Integer id, String nombre, TipoRecurso tipoRecurso, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.tipoRecurso = tipoRecurso;
        this.disponible = disponible;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoRecurso getTipoRecurso() { return tipoRecurso; }
    public void setTipoRecurso(TipoRecurso tipoRecurso) { this.tipoRecurso = tipoRecurso; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}
