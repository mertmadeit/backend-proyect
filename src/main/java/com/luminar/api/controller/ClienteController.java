package com.luminar.api.controller;

import com.luminar.api.domain.Cliente;
import com.luminar.api.repository.ClienteRepository;
import com.luminar.api.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Cliente create(@RequestBody Cliente cliente) {
        cliente.setId(null);
        cliente.setRole("cliente");
        return repository.save(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> update(@PathVariable String id, @RequestBody Cliente details) {
        return repository.findByIdAndRfcIsNotNull(id).map(cliente -> {
            cliente.setNombre(details.getNombre());
            cliente.setRfc(details.getRfc());
            cliente.setDireccion(details.getDireccion());
            cliente.setTelefono(details.getTelefono());
            cliente.setEmail(details.getEmail());
            return ResponseEntity.ok(repository.save(cliente));
        }).orElse(ResponseEntity.notFound().build());
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
