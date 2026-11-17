package cl.duoc.pasteleria.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Un producto del carrito: cuál y cuántas unidades.
public record ItemCompraRequest(
        @NotNull(message = "El producto es obligatorio") Long productoId,
        @NotNull @Min(value = 1, message = "La cantidad mínima es 1") @Max(value = 99, message = "La cantidad máxima es 99") Integer cantidad
) {}