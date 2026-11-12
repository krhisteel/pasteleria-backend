package cl.duoc.pasteleria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Tabla USUARIO del modelo de datos.
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 12)
    private String run;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    // Se guarda encriptada con BCrypt, nunca en texto plano.
    @Column(nullable = false)
    private String password;

    private LocalDate fechaNacimiento;

    @Column(length = 15)
    private String telefono;

    private String region;
    private String comuna;

    @Column(length = 300)
    private String direccion;

    private String codigoPromocional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    private LocalDateTime fechaRegistro;

    // Antes de insertar: completa la fecha de registro y el rol por defecto.
    @PrePersist
    void antesDeGuardar() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (rol == null) rol = Rol.CLIENTE;
    }
}