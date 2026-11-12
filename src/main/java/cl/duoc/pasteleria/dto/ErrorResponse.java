package cl.duoc.pasteleria.dto;

import java.time.LocalDateTime;
import java.util.Map;

// Formato único de error para toda la API (lo pueden usar los otros módulos).
public record ErrorResponse(int status, String mensaje, Map<String, String> errores, LocalDateTime fecha) {
    public static ErrorResponse of(int status, String mensaje) {
        return new ErrorResponse(status, mensaje, null, LocalDateTime.now());
    }
}