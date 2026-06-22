package com.luminar.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int unsigned")
    private Long id;

    @Column(nullable = false)
    private Integer numero;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String detalles;

    @Column(nullable = false)
    private Integer valor;

    @Column(nullable = false)
    private String archivo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idCliente", nullable = false)
    @JsonIgnoreProperties({"createdAt", "updatedAt"})
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idforma", nullable = false)
    private FormaPago formaPago;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idestado", nullable = false)
    private EstadoFactura estadoFactura;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Expose FK IDs for JSON serialization (read from the relationship)
    public Long getIdCliente() {
        return cliente != null ? cliente.getId() : null;
    }

    public Long getIdforma() {
        return formaPago != null ? formaPago.getId() : null;
    }

    public Long getIdestado() {
        return estadoFactura != null ? estadoFactura.getId() : null;
    }
}
