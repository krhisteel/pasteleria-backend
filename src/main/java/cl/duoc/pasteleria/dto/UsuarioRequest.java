package cl.duoc.pasteleria.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

// Crear o editar un usuario desde el panel admin.
// La contraseña es opcional al editar (si viene vacía no se cambia).
public record UsuarioRequest(
        @Size(max = 12) String run,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 50) String nombre,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 100) String apellidos,
        @NotBlank @Email(message = "Correo no válido") @Size(max = 100) String correo,
        @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres") String password,
        @Past LocalDate fechaNacimiento,
        @Size(max = 15) String telefono,
        String region,
        String comuna,
        @Size(max = 300) String direccion,
        String codigoPromocional,
        @NotBlank(message = "El rol es obligatorio") String rol
) {}