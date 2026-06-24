package com.luminar.api.repository;

import com.luminar.api.domain.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    @EntityGraph(attributePaths = {"cliente", "formaPago", "estadoFactura"})
    List<Factura> findAllByOrderByIdDesc();

    boolean existsByClienteId(Long clienteId);

    @Modifying
    @Query(value = "UPDATE consecutivos SET valor = LAST_INSERT_ID(valor + 1) WHERE nombre = 'factura'", nativeQuery = true)
    int reservarSiguienteNumero();

    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    Long obtenerNumeroReservado();
}
