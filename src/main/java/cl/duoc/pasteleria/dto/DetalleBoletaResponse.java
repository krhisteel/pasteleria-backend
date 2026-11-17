package cl.duoc.pasteleria.dto;

import cl.duoc.pasteleria.entity.DetalleBoleta;

// Una línea de la boleta, con los nombres que ya usa el frontend (PagoExito y Pedido).
public record DetalleBoletaResponse(
        Long id,
        String codigo,
        String nombre,
        String imagen,
        int cantidad,
        int precioBase,
        int descuentoAplicado,
        int precioFinal,
        int subtotal
) {
    public static DetalleBoletaResponse from(DetalleBoleta d) {
        return new DetalleBoletaResponse(
                d.getProducto().getId(),
                d.getProducto().getCodigo(),
                d.getNombreProducto(),
                d.getProducto().getImagenUrl(),
                d.getCantidad(),
                d.getPrecioBase(),
                d.getDescuentoAplicado(),
                d.getPrecioFinal(),
                d.getSubtotal()
        );
    }
}