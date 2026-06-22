package com.luminar.api.controller;

import com.luminar.api.domain.Cliente;
import com.luminar.api.domain.EstadoFactura;
import com.luminar.api.domain.Factura;
import com.luminar.api.domain.FormaPago;
import com.luminar.api.dto.FacturaRequest;
import com.luminar.api.repository.ClienteRepository;
import com.luminar.api.repository.EstadoFacturaRepository;
import com.luminar.api.repository.FacturaRepository;
import com.luminar.api.repository.FormaPagoRepository;
import com.luminar.api.service.FacturaPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private String nombreArchivo(Integer numero) {
        return "factura-" + numero + ".pdf";
    }

    @Autowired
    private FacturaRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private FormaPagoRepository formaPagoRepository;

    @Autowired
    private EstadoFacturaRepository estadoFacturaRepository;

    @Autowired
    private FacturaPdfService facturaPdfService;

    @GetMapping
    public List<Factura> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        return repository.findById(id)
                .map(factura -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(
                                HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"factura-" + factura.getNumero() + ".pdf\""
                        )
                        .body(facturaPdfService.generar(factura)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(
            @RequestParam Integer numero,
            @RequestParam(required = false) Long excludeId) {
        List<Factura> all = repository.findAll();
        boolean exists = all.stream().anyMatch(f ->
                f.getNumero().equals(numero) && (excludeId == null || !f.getId().equals(excludeId))
        );
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody FacturaRequest req) {
        Cliente cliente = clienteRepository.findById(req.getIdCliente()).orElse(null);
        FormaPago forma = formaPagoRepository.findById(req.getIdforma()).orElse(null);
        EstadoFactura estado = estadoFacturaRepository.findById(req.getIdestado()).orElse(null);

        if (cliente == null || forma == null || estado == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Referencia no encontrada"));
        }

        Factura factura = new Factura();
        factura.setNumero(req.getNumero());
        factura.setDetalles(req.getDetalles());
        factura.setValor(req.getValor());
        factura.setArchivo(nombreArchivo(req.getNumero()));
        factura.setCliente(cliente);
        factura.setFormaPago(forma);
        factura.setEstadoFactura(estado);

        return ResponseEntity.ok(repository.save(factura));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody FacturaRequest req) {
        return repository.findById(id).map(factura -> {
            Cliente cliente = clienteRepository.findById(req.getIdCliente()).orElse(null);
            FormaPago forma = formaPagoRepository.findById(req.getIdforma()).orElse(null);
            EstadoFactura estado = estadoFacturaRepository.findById(req.getIdestado()).orElse(null);

            if (cliente == null || forma == null || estado == null) {
                return ResponseEntity.badRequest().body((Object) Map.of("error", "Referencia no encontrada"));
            }

            factura.setNumero(req.getNumero());
            factura.setDetalles(req.getDetalles());
            factura.setValor(req.getValor());
            factura.setArchivo(nombreArchivo(req.getNumero()));
            factura.setCliente(cliente);
            factura.setFormaPago(forma);
            factura.setEstadoFactura(estado);

            return ResponseEntity.ok((Object) repository.save(factura));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repository.findById(id).map(factura -> {
            repository.delete(factura);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
