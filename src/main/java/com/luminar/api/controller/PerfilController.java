package com.luminar.api.controller;

import com.luminar.api.domain.Perfil;
import com.luminar.api.repository.PerfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfiles")
public class PerfilController {

    @Autowired
    private PerfilRepository repository;

    @GetMapping
    public List<Perfil> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Perfil create(@RequestBody Perfil perfil) {
        return repository.save(perfil);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Perfil> update(@PathVariable Long id, @RequestBody Perfil details) {
        return repository.findById(id).map(perfil -> {
            perfil.setNombre(details.getNombre());
            return ResponseEntity.ok(repository.save(perfil));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repository.findById(id).map(perfil -> {
            repository.delete(perfil);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
