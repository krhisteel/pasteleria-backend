package cl.duoc.pasteleria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Tabla BOLETA: una compra confirmada. Relación USUARIO (1) -> BOLETA (N).
@Entity
@Table(name = "boleta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cliente que hizo la compra.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    // pendiente, procesando, enviado, completado o cancelado (los mismos textos que usa el frontend).
    @Column(nullable = false, length = 20)
    private String estado;

    // Datos de despacho.
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 300)
    private String direccion;

    private String region;
    private String comuna;

    @Column(length = 300)
    private String indicaciones;

    @Column(length = 20)
    private String metodoPago;

    // Montos en pesos, calculados por el servidor.
    @Column(nullable = false)
    private Integer descuentoUsuario;

    @Column(nullable = false)
    private Integer subtotal;

    @Column(nullable = false)
    private Integer ahorro;

    @Column(nullable = false)
    private Integer total;

    // Líneas de la boleta. cascade = ALL: al guardar la boleta se guardan sus detalles.
    @Builder.Default
    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleBoleta> detalles = new ArrayList<>();

    // Antes de insertar: fecha actual y estado inicial.
    @PrePersist
    void antesDeGuardar() {
        if (fecha == null) fecha = LocalDateTime.now();
        if (estado == null) estado = "pendiente";
    }
}