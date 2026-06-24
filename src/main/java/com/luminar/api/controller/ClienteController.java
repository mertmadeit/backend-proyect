package com.luminar.api.controller;

import com.luminar.api.domain.Cliente;
import com.luminar.api.repository.ClienteRepository;
import com.luminar.api.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private FacturaRepository facturaRepository;

    @GetMapping
    public List<Cliente> getAll() {
        return repository.findAllByRfcIsNotNullOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getById(@PathVariable String id) {
        return repository.findByIdAndRfcIsNotNull(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Cliente cliente) {
        normalizar(cliente);
        if (!datosValidos(cliente)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Completa todos los datos del cliente"));
        }
        if (repository.existsByEmailIgnoreCase(cliente.getEmail())) {
            return ResponseEntity.status(409).body(Map.of("error", "El correo ya está registrado"));
        }
        cliente.setId(null);
        cliente.setRole("cliente");
        return ResponseEntity.ok(repository.save(cliente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Cliente details) {
        normalizar(details);
        if (!datosValidos(details)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Completa todos los datos del cliente"));
        }
        if (repository.existsByEmailIgnoreCaseAndIdNot(details.getEmail(), id)) {
            return ResponseEntity.status(409).body(Map.of("error", "El correo ya pertenece a otro usuario"));
        }
        return repository.findByIdAndRfcIsNotNull(id).map(cliente -> {
            cliente.setNombre(details.getNombre());
            cliente.setRfc(details.getRfc());
            cliente.setDireccion(details.getDireccion());
            cliente.setTelefono(details.getTelefono());
            cliente.setEmail(details.getEmail());
            return ResponseEntity.ok((Object) repository.save(cliente));
        }).orElse(ResponseEntity.notFound().build());
    }

    private void normalizar(Cliente cliente) {
        if (cliente.getNombre() != null) cliente.setNombre(cliente.getNombre().trim());
        if (cliente.getEmail() != null) cliente.setEmail(cliente.getEmail().trim().toLowerCase());
        if (cliente.getRfc() != null) cliente.setRfc(cliente.getRfc().trim().toUpperCase());
        if (cliente.getDireccion() != null) cliente.setDireccion(cliente.getDireccion().trim());
        if (cliente.getTelefono() != null) cliente.setTelefono(cliente.getTelefono().trim());
    }

    private boolean datosValidos(Cliente cliente) {
        return cliente.getNombre() != null && !cliente.getNombre().isBlank()
                && cliente.getEmail() != null && !cliente.getEmail().isBlank()
                && cliente.getRfc() != null && !cliente.getRfc().isBlank()
                && cliente.getDireccion() != null && !cliente.getDireccion().isBlank()
                && cliente.getTelefono() != null && !cliente.getTelefono().isBlank();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return repository.findByIdAndRfcIsNotNull(id).map(cliente -> {
            if (facturaRepository.existsByClienteId(id)) {
                return ResponseEntity.status(409).body("El cliente tiene facturas relacionadas");
            }
            if ("cliente".equals(cliente.getRole())) {
                repository.delete(cliente);
            } else {
                cliente.setRfc(null);
                cliente.setDireccion(null);
                cliente.setTelefono(null);
                repository.save(cliente);
            }
            return ResponseEntity.ok().body("Cliente eliminado");
        }).orElse(ResponseEntity.notFound().build());
    }
}
