package cl.duoc.pasteleria.dto;

import cl.duoc.pasteleria.entity.Boleta;
import cl.duoc.pasteleria.entity.DetalleBoleta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// La boleta que devuelve la API. Tiene la misma forma que la orden que usaba el frontend,
// así PagoExito y Pedido la muestran sin cambios grandes.
public record BoletaResponse(
        Long id,
        LocalDateTime fecha,
        String estado,
        String nombre,
        String email,
        String telefono,
        String direccion,
        String region,
        String comuna,
        String indicaciones,
        String metodoPago,
        boolean pagoSimulado,
        int descuentoUsuario,
        int subtotal,
        int ahorro,
        int total,
        List<DetalleBoletaResponse> productos
) {
    public static BoletaResponse from(Boleta b) {
        List<DetalleBoletaResponse> productos = new ArrayList<>();
        for (DetalleBoleta d : b.getDetalles()) {
            productos.add(DetalleBoletaResponse.from(d));
        }
        return new BoletaResponse(
                b.getId(), b.getFecha(), b.getEstado(),
                b.getNombre(), b.getUsuario().getCorreo(), b.getTelefono(),
                b.getDireccion(), b.getRegion(), b.getComuna(),
                b.getIndicaciones(), b.getMetodoPago(),
                true,
                b.getDescuentoUsuario(), b.getSubtotal(), b.getAhorro(), b.getTotal(),
                productos
        );
    }
}