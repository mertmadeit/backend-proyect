package com.luminar.api.service;

import com.luminar.api.domain.Factura;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FacturaPdfService {

    private static final float MARGEN = 54;
    private static final float ANCHO_TEXTO = PDRectangle.A4.getWidth() - (MARGEN * 2);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private final PDType1Font fuenteNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font fuenteNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public byte[] generar(Factura factura) {
        try (
                PDDocument documento = new PDDocument();
                ByteArrayOutputStream salida = new ByteArrayOutputStream()
        ) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                float y = pagina.getMediaBox().getHeight() - MARGEN;

                escribir(contenido, "LUMINAR", MARGEN, y, 20, true);
                y -= 26;
                escribir(contenido, "FACTURA #" + factura.getNumero(), MARGEN, y, 15, true);
                y -= 18;

                String fecha = factura.getCreatedAt() == null
                        ? "Sin fecha registrada"
                        : factura.getCreatedAt().format(FORMATO_FECHA);
                escribir(contenido, "Fecha: " + fecha, MARGEN, y, 10, false);
                y -= 28;

                linea(contenido, y);
                y -= 24;
                escribir(contenido, "DATOS DEL CLIENTE", MARGEN, y, 11, true);
                y -= 18;
                escribir(contenido, "Nombre: " + factura.getCliente().getNombre(), MARGEN, y, 10, false);
                y -= 15;
                escribir(contenido, "RFC: " + factura.getCliente().getRfc(), MARGEN, y, 10, false);
                y -= 15;
                escribir(contenido, "Correo: " + factura.getCliente().getEmail(), MARGEN, y, 10, false);
                y -= 15;
                escribir(contenido, "Telefono: " + factura.getCliente().getTelefono(), MARGEN, y, 10, false);
                y -= 15;
                escribir(contenido, "Direccion: " + factura.getCliente().getDireccion(), MARGEN, y, 10, false);
                y -= 28;

                linea(contenido, y);
                y -= 24;
                escribir(contenido, "INFORMACION DE LA FACTURA", MARGEN, y, 11, true);
                y -= 18;
                escribir(contenido, "Forma de pago: " + factura.getFormaPago().getNombre(), MARGEN, y, 10, false);
                y -= 15;
                escribir(contenido, "Estado: " + factura.getEstadoFactura().getEstado(), MARGEN, y, 10, false);
                y -= 24;
                escribir(contenido, "Detalles:", MARGEN, y, 10, true);
                y -= 15;

                for (String linea : dividir(factura.getDetalles(), 10)) {
                    escribir(contenido, linea, MARGEN, y, 10, false);
                    y -= 14;
                }

                y -= 20;
                linea(contenido, y);
                y -= 30;
                escribirDerecha(
                        contenido,
                        "TOTAL: " + FORMATO_MONEDA.format(factura.getValor()),
                        pagina.getMediaBox().getWidth() - MARGEN,
                        y,
                        16
                );

                escribir(contenido, "Documento generado por el sistema Luminar.", MARGEN, MARGEN, 9, false);
            }

            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el PDF de la factura", exception);
        }
    }

    private void escribir(
            PDPageContentStream contenido,
            String texto,
            float x,
            float y,
            float tamano,
            boolean negrita
    ) throws IOException {
        contenido.beginText();
        contenido.setFont(negrita ? fuenteNegrita : fuenteNormal, tamano);
        contenido.newLineAtOffset(x, y);
        contenido.showText(limpiar(texto));
        contenido.endText();
    }

    private void escribirDerecha(
            PDPageContentStream contenido,
            String texto,
            float xDerecha,
            float y,
            float tamano
    ) throws IOException {
        String limpio = limpiar(texto);
        float ancho = fuenteNegrita.getStringWidth(limpio) / 1000 * tamano;
        escribir(contenido, limpio, xDerecha - ancho, y, tamano, true);
    }

    private void linea(PDPageContentStream contenido, float y) throws IOException {
        contenido.setLineWidth(0.7f);
        contenido.moveTo(MARGEN, y);
        contenido.lineTo(PDRectangle.A4.getWidth() - MARGEN, y);
        contenido.stroke();
    }

    private List<String> dividir(String texto, float tamano) throws IOException {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();

        for (String palabra : limpiar(texto).split("\\s+")) {
            String candidato = actual.isEmpty() ? palabra : actual + " " + palabra;
            float ancho = fuenteNormal.getStringWidth(candidato) / 1000 * tamano;

            if (ancho > ANCHO_TEXTO && !actual.isEmpty()) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(candidato);
            }
        }

        if (!actual.isEmpty()) lineas.add(actual.toString());
        return lineas;
    }

    private String limpiar(String texto) {
        if (texto == null) return "";
        return texto.replaceAll("[^\\x20-\\xFF]", "?");
    }
}
