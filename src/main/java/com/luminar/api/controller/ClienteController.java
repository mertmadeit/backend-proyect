package com.luminar.api.controller;

import com.luminar.api.domain.Cliente;
import com.luminar.api.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository repository;

    @GetMapping
    public List<Cliente> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Cliente create(@RequestBody Cliente cliente) {
        return repository.save(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> update(@PathVariable Long id, @RequestBody Cliente details) {
        return repository.findById(id).map(cliente -> {
            cliente.setNombre(details.getNombre());
            cliente.setRfc(details.getRfc());
            cliente.setDireccion(details.getDireccion());
            cliente.setTelefono(details.getTelefono());
            cliente.setEmail(details.getEmail());
            return ResponseEntity.ok(repository.save(cliente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repository.findById(id).map(cliente -> {
            repository.delete(cliente);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
