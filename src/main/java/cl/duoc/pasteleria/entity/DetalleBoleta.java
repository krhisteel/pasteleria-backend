package cl.duoc.pasteleria.entity;

import jakarta.persistence.*;
import lombok.*;

// Tabla DETALLE_BOLETA: una línea de la compra (un producto y su cantidad).
// Relaciones: BOLETA (1) -> DETALLE_BOLETA (N) y PRODUCTO (1) -> DETALLE_BOLETA (N).
@Entity
@Table(name = "detalle_boleta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boleta_id", nullable = false)
    private Boleta boleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Copia del nombre y los precios al momento de comprar:
    // si después cambia el producto, la boleta no cambia.
    @Column(nullable = false, length = 150)
    private String nombreProducto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Integer precioBase;

    // Porcentaje aplicado: el mayor entre la oferta del producto y el beneficio del usuario.
    @Column(nullable = false)
    private Integer descuentoAplicado;

    @Column(nullable = false)
    private Integer precioFinal;

    @Column(nullable = false)
    private Integer subtotal;
}