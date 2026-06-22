package com.luminar.api.controller;

import com.luminar.api.domain.FormaPago;
import com.luminar.api.repository.FormaPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formaspago")
public class FormaPagoController {

    @Autowired
    private FormaPagoRepository repository;

    @GetMapping
    public List<FormaPago> getAll() {
        return repository.findAll();
    }
}
