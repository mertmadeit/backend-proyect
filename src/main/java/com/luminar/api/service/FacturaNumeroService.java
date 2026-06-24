package com.luminar.api.service;

import com.luminar.api.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacturaNumeroService {

    private final FacturaRepository facturaRepository;

    public FacturaNumeroService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    @Transactional
    public Integer siguienteNumero() {
        int actualizados = facturaRepository.reservarSiguienteNumero();
        if (actualizados != 1) {
            throw new IllegalStateException("No fue posible reservar el folio de la factura");
        }

        return Math.toIntExact(facturaRepository.obtenerNumeroReservado());
    }
}
