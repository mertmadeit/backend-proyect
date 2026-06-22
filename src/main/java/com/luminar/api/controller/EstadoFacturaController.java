package com.luminar.api.controller;

import com.luminar.api.domain.EstadoFactura;
import com.luminar.api.repository.EstadoFacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estadosfacturas")
public class EstadoFacturaController {

    @Autowired
    private EstadoFacturaRepository repository;

    @GetMapping
    public List<EstadoFactura> getAll() {
        return repository.findAll();
    }
}
