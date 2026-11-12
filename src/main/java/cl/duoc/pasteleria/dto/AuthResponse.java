package cl.duoc.pasteleria.dto;

// Respuesta del login y del refresh: tokens + datos del usuario.
public record AuthResponse(
        String token,
        String refreshToken,
        String tipoToken,
        long expiraEnMs,
        UsuarioResponse usuario
) {}