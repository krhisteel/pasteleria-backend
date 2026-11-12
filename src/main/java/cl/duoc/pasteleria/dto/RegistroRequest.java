package cl.duoc.pasteleria.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

// Datos del formulario de Registro (siempre crea un CLIENTE).
public record RegistroRequest(
        @Size(max = 12) String run,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 50) String nombre,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 100) String apellidos,
        @NotBlank @Email(message = "Correo no válido") @Size(max = 100) String correo,
        @NotBlank @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres") String password,
        @Past(message = "La fecha de nacimiento debe ser pasada") LocalDate fechaNacimiento,
        @Size(max = 20) String telefono,
        String region,
        String comuna,
        @Size(max = 300) String direccion,
        String codigoPromocional
) {}