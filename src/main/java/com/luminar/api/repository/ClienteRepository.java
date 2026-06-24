package com.luminar.api.repository;

import com.luminar.api.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    List<Cliente> findAllByRfcIsNotNullOrderByCreatedAtDesc();

    Optional<Cliente> findByIdAndRfcIsNotNull(String id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, String id);
}
