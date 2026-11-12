package cl.duoc.pasteleria.dto;

import java.util.List;

// Resultado del cálculo de beneficios de un usuario.
public record Beneficios(int descuentoEdad, int descuentoCodigo, boolean tortaGratisDuoc, List<String> beneficios) {}