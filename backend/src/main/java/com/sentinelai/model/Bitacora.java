package com.sentinelai.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora")
public class Bitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incidente_id", nullable = false)
    private Incidente incidente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_anterior_id")
    private Estado estadoAnterior;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado_nuevo_id", nullable = false)
    private Estado estadoNuevo;

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Bitacora() { }

    public Bitacora(Incidente incidente, Usuario usuario, Estado estadoAnterior, Estado estadoNuevo, String comentario) {
        this.incidente = incidente;
        this.usuario = usuario;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.comentario = comentario;
        this.fecha = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public Incidente getIncidente() { return incidente; }
    public Usuario getUsuario() { return usuario; }
    public Estado getEstadoAnterior() { return estadoAnterior; }
    public Estado getEstadoNuevo() { return estadoNuevo; }
    public String getComentario() { return comentario; }
    public LocalDateTime getFecha() { return fecha; }
}
