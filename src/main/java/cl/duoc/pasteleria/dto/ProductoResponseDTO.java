
package cl.duoc.pasteleria.dto;

import cl.duoc.pasteleria.entity.Producto;
import java.math.BigDecimal;

public record ProductoResponseDTO(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        Integer stockCritico,
        BigDecimal descuento,
        String imagenUrl,
        Boolean activo,
        CategoriaDTO categoria
) {

    public record CategoriaDTO(
            Long id,
            String nombre
    ) {}

    public static ProductoResponseDTO desdeEntidad(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getStockCritico(),
                producto.getDescuento(),
                producto.getImagenUrl(),
                producto.getActivo(),
                new CategoriaDTO(
                        producto.getCategoria().getId(),
                        producto.getCategoria().getNombre()
                )
        );
    }
}
