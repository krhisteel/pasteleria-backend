package cl.duoc.pasteleria.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

// Lo que envía el checkout: productos del carrito + datos de despacho.
// No trae precios: el servidor los calcula para que nadie pueda cambiarlos desde el navegador.
public record CompraRequest(
        @NotEmpty(message = "El carrito está vacío") @Valid List<ItemCompraRequest> items,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 150) String nombre,
        @Size(max = 20) String telefono,
        @NotBlank(message = "La dirección es obligatoria") @Size(max = 300) String direccion,
        String region,
        String comuna,
        @Size(max = 300) String indicaciones,
        @Size(max = 20) String metodoPago
) {}