package cl.duoc.pasteleria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_compra", uniqueConstraints = {
        @UniqueConstraint(name = "uk_solicitud_usuario_clave", columnNames = {"usuario_id", "clave"}),
        @UniqueConstraint(name = "uk_solicitud_boleta", columnNames = "boleta_id")
})
@Getter
@Setter
@NoArgsConstructor
public class SolicitudCompra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_solicitud_usuario"))
    private Usuario usuario;

    @NotBlank @Pattern(regexp = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    @Column(nullable = false, length = 36, updatable = false)
    private String clave;

    @NotBlank @Pattern(regexp = "[0-9a-f]{64}")
    @Column(nullable = false, length = 64, updatable = false)
    private String huella;

    @NotNull @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    // Se enlaza antes de confirmar la futura transaccion de compra.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boleta_id", unique = true,
            foreignKey = @ForeignKey(name = "fk_solicitud_boleta"))
    private Boleta boleta;
}
