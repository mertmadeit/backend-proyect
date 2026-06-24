package com.luminar.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FacturaRequest {
    private Long id;
    private String detalles;
    private Integer valor;

    @JsonProperty("idCliente")
    private Long idCliente;

    @JsonProperty("idforma")
    private Long idforma;

    @JsonProperty("idestado")
    private Long idestado;
}
