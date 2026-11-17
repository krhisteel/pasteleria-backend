package cl.duoc.pasteleria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "boleta", indexes = {
        @Index(name = "ix_boleta_usuario_fecha", columnList = "usuario_id,fecha,id"),
        @Index(name = "ix_boleta_estado_fecha", columnList = "estado,fecha,id")
})
@Getter
@Setter
@NoArgsConstructor
public class Boleta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_boleta_usuario"))
    private Usuario usuario;

    @Version @NotNull @Min(0)
    @Column(nullable = false)
    private Long version = 0L;

    @NotNull @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @NotNull @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private EstadoBoleta estado = EstadoBoleta.PENDIENTE;

    @NotBlank @Size(max = 150) @Column(nullable = false, length = 150, updatable = false)
    private String nombre;

    @NotBlank @Email @Size(max = 100) @Column(nullable = false, length = 100, updatable = false)
    private String email;

    @NotBlank @Size(max = 30)
    @Column(nullable = false, length = 30, updatable = false)
    private String telefono;

    @NotBlank @Size(max = 500) @Column(nullable = false, length = 500, updatable = false)
    private String direccion;

    @NotBlank @Size(max = 100) @Column(nullable = false, length = 100, updatable = false)
    private String region;

    @NotBlank @Size(max = 100) @Column(nullable = false, length = 100, updatable = false)
    private String comuna;

    @Size(max = 300) @Column(length = 300, updatable = false)
    private String indicaciones;

    @NotBlank @Pattern(regexp = "webpay|transferencia")
    @Column(name = "metodo_pago", nullable = false, length = 20, updatable = false)
    private String metodoPago;

    @NotNull @AssertTrue
    @Column(name = "pago_simulado", nullable = false, updatable = false)
    private Boolean pagoSimulado = true;

    @NotNull @DecimalMin("0") @DecimalMax("100") @Digits(integer = 3, fraction = 2)
    @Column(name = "descuento_usuario", nullable = false, precision = 5, scale = 2, updatable = false)
    private BigDecimal descuentoUsuario;

    @NotNull @DecimalMin("0") @Digits(integer = 14, fraction = 2)
    @Column(nullable = false, precision = 16, scale = 2, updatable = false)
    private BigDecimal subtotal;

    // El redondeo al peso puede superar el precio base con centavos.
    @NotNull @Digits(integer = 14, fraction = 2)
    @Column(nullable = false, precision = 16, scale = 2, updatable = false)
    private BigDecimal ahorro;

    @NotNull @DecimalMin("0") @Digits(integer = 14, fraction = 2)
    @Column(nullable = false, precision = 16, scale = 2, updatable = false)
    private BigDecimal total;

    @NotNull @Column(name = "stock_descontado", nullable = false, updatable = false)
    private Boolean stockDescontado = false;

    @NotNull @Column(name = "stock_restituido", nullable = false)
    private Boolean stockRestituido = false;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    // No REMOVE, ALL ni orphanRemoval: conservar siempre el historial.
    @OneToMany(mappedBy = "boleta", cascade = CascadeType.PERSIST)
    private List<DetalleBoleta> detalles = new ArrayList<>();
}
