package cl.duoc.pasteleria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Datos que envía el frontend para iniciar sesión.
public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo no válido") String correo,
        @NotBlank(message = "La contraseña es obligatoria") String password
) {}