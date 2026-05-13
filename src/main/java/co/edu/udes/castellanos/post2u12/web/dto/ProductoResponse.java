package co.edu.udes.castellanos.post2u12.web.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock) {
}
