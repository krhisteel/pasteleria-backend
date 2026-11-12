package cl.duoc.pasteleria.dto;

import cl.duoc.pasteleria.entity.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Lo que el backend devuelve de un usuario. Nunca incluye la contraseña.
public record UsuarioResponse(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String correo,
        String email,
        LocalDate fechaNacimiento,
        String telefono,
        String region,
        String comuna,
        String direccion,
        String codigoPromocional,
        String rol,
        String tipo,
        int descuentoEdad,
        int descuentoCodigo,
        boolean tortaGratisDuoc,
        List<String> beneficios,
        LocalDateTime fechaRegistro
) {
    // Convierte la entidad Usuario (base de datos) en la respuesta para el frontend.
    public static UsuarioResponse from(Usuario u, Beneficios b) {
        return new UsuarioResponse(
                u.getId(), u.getRun(), u.getNombre(), u.getApellidos(),
                u.getCorreo(), u.getCorreo(), u.getFechaNacimiento(),
                u.getTelefono(), u.getRegion(), u.getComuna(), u.getDireccion(),
                u.getCodigoPromocional(),
                u.getRol().getClave(), u.getRol().getNombre(),
                b.descuentoEdad(), b.descuentoCodigo(), b.tortaGratisDuoc(), b.beneficios(),
                u.getFechaRegistro()
        );
    }
}