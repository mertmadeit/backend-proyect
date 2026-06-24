package com.luminar.api.repository;

import com.luminar.api.domain.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    @EntityGraph(attributePaths = {"cliente", "formaPago", "estadoFactura"})
    List<Factura> findAllByOrderByIdDesc();

    boolean existsByNumero(Integer numero);

    boolean existsByNumeroAndIdNot(Integer numero, Long id);
}
