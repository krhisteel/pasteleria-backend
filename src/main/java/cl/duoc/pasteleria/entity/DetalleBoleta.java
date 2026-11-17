package cl.duoc.pasteleria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_boleta", uniqueConstraints = {
        @UniqueConstraint(name = "uk_detalle_boleta_producto", columnNames = {"boleta_id", "producto_id"})
}, indexes = @Index(name = "ix_detalle_producto", columnList = "producto_id"))
@Getter
@Setter
@NoArgsConstructor
public class DetalleBoleta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "boleta_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_boleta"))
    private Boleta boleta;

    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_producto"))
    private Producto producto;

    @NotBlank @Size(max = 20) @Column(nullable = false, length = 20, updatable = false)
    private String codigo;

    @NotBlank @Size(max = 150) @Column(nullable = false, length = 150, updatable = false)
    private String nombre;

    @NotBlank @Size(max = 500) @Column(nullable = false, length = 500, updatable = false)
    private String imagen;

    @NotNull @Min(1) @Max(50) @Column(nullable = false, updatable = false)
    private Integer cantidad;

    @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 14, fraction = 2)
    @Column(name = "precio_base", nullable = false, precision = 16, scale = 2, updatable = false)
    private BigDecimal precioBase;

    @NotNull @DecimalMin("0") @DecimalMax("100") @Digits(integer = 3, fraction = 2)
    @Column(name = "descuento_aplicado", nullable = false, precision = 5, scale = 2, updatable = false)
    private BigDecimal descuentoAplicado;

    @NotNull @DecimalMin("0") @Digits(integer = 14, fraction = 2)
    @Column(name = "precio_final", nullable = false, precision = 16, scale = 2, updatable = false)
    private BigDecimal precioFinal;

    @NotNull @DecimalMin("0") @Digits(integer = 14, fraction = 2)
    @Column(nullable = false, precision = 16, scale = 2, updatable = false)
    private BigDecimal subtotal;
}
