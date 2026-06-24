package com.luminar.api.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "user")
public class Cliente {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "name", nullable = false)
    private String nombre;

    @Column
    private String rfc;

    @Column
    private String direccion;

    @Column
    private String telefono;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 32)
    private String role = "cliente";

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prepararCliente() {
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        if (role == null || role.isBlank()) role = "cliente";
    }
}
