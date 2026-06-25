package com.luminar.api.service;

import com.luminar.api.domain.Factura;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class FacturaPdfService {

    private static final float MARGEN = 54;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private static final Color COLOR_PRIMARIO = new Color(17, 24, 39);      // Slate-900
    private static final Color COLOR_SECUNDARIO = new Color(107, 114, 128);  // Gray-500
    private static final Color COLOR_FONDO_BOX = new Color(243, 244, 246);   // Gray-100
    private static final Color COLOR_BORDE = new Color(229, 231, 235);       // Gray-200
    private static final Color COLOR_TABLA_CABECERA = new Color(17, 24, 39); // Slate-900
    private static final Color COLOR_TABLA_FILA_ALT = new Color(249, 250, 251); // Gray-50

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
                float anchoPagina = pagina.getMediaBox().getWidth();

                // --- COLUMNA IZQUIERDA (LOGOTIPO Y EMISOR) ---
                escribir(contenido, "LUMINAR", MARGEN, y - 10, 22, true, COLOR_PRIMARIO);
                escribir(contenido, "Curaduría fotográfica y arte", MARGEN, y - 24, 8, false, COLOR_SECUNDARIO);

                escribir(contenido, "Luminar S.A. de C.V.", MARGEN, y - 50, 9, false, COLOR_PRIMARIO);
                escribir(contenido, "RFC: LUM120304AA1", MARGEN, y - 62, 9, false, COLOR_SECUNDARIO);
                escribir(contenido, "Av. Paseo de la Reforma 405, CDMX", MARGEN, y - 74, 9, false, COLOR_SECUNDARIO);
                escribir(contenido, "contacto@luminar.com", MARGEN, y - 86, 9, false, COLOR_SECUNDARIO);

                // --- COLUMNA DERECHA (TARJETA DE FACTURA) ---
                float cajaX = 350;
                float cajaAncho = anchoPagina - MARGEN - cajaX;
                float cajaAlto = 100;
                float cajaY = y - cajaAlto + 10;

                rectangulo(contenido, cajaX, cajaY, cajaAncho, cajaAlto, COLOR_FONDO_BOX);

                escribir(contenido, "FACTURA", cajaX + 15, cajaY + cajaAlto - 22, 9, true, COLOR_SECUNDARIO);
                escribir(contenido, "Folio: #" + String.format("%05d", factura.getNumero()), cajaX + 15, cajaY + cajaAlto - 40, 14, true, COLOR_PRIMARIO);

                String fecha = factura.getCreatedAt() == null
                        ? "Sin fecha registrada"
                        : factura.getCreatedAt().format(FORMATO_FECHA);
                escribir(contenido, "Fecha: " + fecha, cajaX + 15, cajaY + cajaAlto - 60, 8, false, COLOR_SECUNDARIO);

                // Insignia de Estado
                String estado = factura.getEstadoFactura().getEstado().toUpperCase(Locale.ROOT);
                Color colorEstado = COLOR_SECUNDARIO;
                if (estado.contains("PAGAD")) {
                    colorEstado = new Color(4, 120, 87); // Emerald-700
                } else if (estado.contains("CANCELAD")) {
                    colorEstado = new Color(185, 28, 28); // Red-700
                } else {
                    colorEstado = new Color(29, 78, 216); // Blue-700
                }
                rectangulo(contenido, cajaX + 15, cajaY + 12, 75, 16, colorEstado);
                escribir(contenido, estado, cajaX + 22, cajaY + 16, 7, true, Color.WHITE);

                // --- SECCIÓN DE CLIENTE ---
                y -= 120;

                // Barra de acento vertical
                rectangulo(contenido, MARGEN, y - 55, 3, 65, COLOR_PRIMARIO);

                escribir(contenido, "RECEPTOR / CLIENTE", MARGEN + 10, y, 9, true, COLOR_SECUNDARIO);
                escribir(contenido, factura.getCliente().getNombre(), MARGEN + 10, y - 18, 12, true, COLOR_PRIMARIO);
                escribir(contenido, "RFC: " + factura.getCliente().getRfc(), MARGEN + 10, y - 32, 9, false, COLOR_SECUNDARIO);
                escribir(contenido, "Dirección: " + factura.getCliente().getDireccion(), MARGEN + 10, y - 44, 9, false, COLOR_SECUNDARIO);
                escribir(contenido, "Teléfono: " + factura.getCliente().getTelefono() + "  |  Email: " + factura.getCliente().getEmail(), MARGEN + 10, y - 56, 9, false, COLOR_SECUNDARIO);

                // --- TABLA DE DETALLES ---
                y -= 90;

                // Cabecera de Tabla
                float tablaY = y;
                float tablaAlto = 24;
                float tablaAncho = anchoPagina - (MARGEN * 2);
                rectangulo(contenido, MARGEN, tablaY, tablaAncho, tablaAlto, COLOR_TABLA_CABECERA);

                escribir(contenido, "DESCRIPCIÓN / CONCEPTO", MARGEN + 12, tablaY + 7, 9, true, Color.WHITE);
                escribirDerecha(contenido, "IMPORTE (MXN)", anchoPagina - MARGEN - 12, tablaY + 7, 9, Color.WHITE, true);

                y -= 24;

                // Filas de Detalles
                String[] lineas = factura.getDetalles().split("\n");
                boolean filaAlterna = false;

                for (String lineaTexto : lineas) {
                    if (lineaTexto.trim().isEmpty()) continue;

                    float filaAlto = 22;
                    if (filaAlterna) {
                        rectangulo(contenido, MARGEN, y - filaAlto, tablaAncho, filaAlto, COLOR_TABLA_FILA_ALT);
                    }

                    // Separar concepto e importe
                    String concepto = lineaTexto;
                    String importe = "";
                    int guionIdx = lineaTexto.lastIndexOf(" - ");
                    if (guionIdx != -1) {
                        concepto = lineaTexto.substring(0, guionIdx).trim();
                        importe = lineaTexto.substring(guionIdx + 3).trim();
                    }

                    escribir(contenido, concepto, MARGEN + 12, y - 15, 9, false, COLOR_PRIMARIO);
                    if (!importe.isEmpty()) {
                        escribirDerecha(contenido, importe, anchoPagina - MARGEN - 12, y - 15, 9, COLOR_PRIMARIO, false);
                    }

                    y -= filaAlto;
                    filaAlterna = !filaAlterna;
                }

                // --- TOTAL ---
                y -= 20;
                linea(contenido, y, COLOR_BORDE);

                y -= 25;
                escribir(contenido, "Método de Pago: " + factura.getFormaPago().getNombre(), MARGEN, y + 5, 9, false, COLOR_SECUNDARIO);

                String totalText = "TOTAL: " + FORMATO_MONEDA.format(factura.getValor());
                escribirDerecha(contenido, totalText, anchoPagina - MARGEN, y, 15, COLOR_PRIMARIO, true);

                // --- PIE DE PÁGINA ---
                escribirCentrado(contenido, "Este documento es una representación impresa de un comprobante emitido por Luminar.", anchoPagina / 2, 70, 8, COLOR_SECUNDARIO);
                escribirCentrado(contenido, "Luminar Curaduría Fotográfica - Gracias por su confianza.", anchoPagina / 2, 54, 8, COLOR_SECUNDARIO);
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
            boolean negrita,
            Color color
    ) throws IOException {
        contenido.beginText();
        contenido.setFont(negrita ? fuenteNegrita : fuenteNormal, tamano);
        contenido.setNonStrokingColor(color);
        contenido.newLineAtOffset(x, y);
        contenido.showText(limpiar(texto));
        contenido.endText();
    }

    private void escribirDerecha(
            PDPageContentStream contenido,
            String texto,
            float xDerecha,
            float y,
            float tamano,
            Color color,
            boolean negrita
    ) throws IOException {
        String limpio = limpiar(texto);
        float ancho = (negrita ? fuenteNegrita : fuenteNormal).getStringWidth(limpio) / 1000 * tamano;
        escribir(contenido, limpio, xDerecha - ancho, y, tamano, negrita, color);
    }

    private void escribirCentrado(
            PDPageContentStream contenido,
            String texto,
            float xCentro,
            float y,
            float tamano,
            Color color
    ) throws IOException {
        String limpio = limpiar(texto);
        float ancho = fuenteNormal.getStringWidth(limpio) / 1000 * tamano;
        escribir(contenido, limpio, xCentro - (ancho / 2), y, tamano, false, color);
    }

    private void rectangulo(
            PDPageContentStream contenido,
            float x,
            float y,
            float ancho,
            float alto,
            Color color
    ) throws IOException {
        contenido.setNonStrokingColor(color);
        contenido.addRect(x, y, ancho, alto);
        contenido.fill();
    }

    private void linea(PDPageContentStream contenido, float y, Color color) throws IOException {
        contenido.setLineWidth(0.7f);
        contenido.setStrokingColor(color);
        contenido.moveTo(MARGEN, y);
        contenido.lineTo(PDRectangle.A4.getWidth() - MARGEN, y);
        contenido.stroke();
    }

    private String limpiar(String texto) {
        if (texto == null) return "";
        return texto.replaceAll("[^\\x20-\\xFF]", "?");
    }
}
