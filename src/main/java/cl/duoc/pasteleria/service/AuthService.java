package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.*;
import cl.duoc.pasteleria.entity.Usuario;
import cl.duoc.pasteleria.exception.RecursoNoEncontradoException;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import cl.duoc.pasteleria.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    // 1) Spring compara la contraseña con el hash BCrypt. 2) Si es correcta, genera los tokens.
    public AuthResponse login(LoginRequest req) {
        String correo = req.correo().trim().toLowerCase();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, req.password()));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Correo o contraseña incorrectos.");
        }
        Usuario u = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos."));
        return crearRespuesta(u);
    }

    // El registro público siempre crea un CLIENTE.
    public UsuarioResponse registrar(RegistroRequest r) {
        return usuarioService.crear(new UsuarioRequest(
                r.run(), r.nombre(), r.apellidos(), r.correo(), r.password(),
                r.fechaNacimiento(), r.telefono(), r.region(), r.comuna(),
                r.direccion(), r.codigoPromocional(), "cliente"));
    }

    // Renovación: recibe el refresh token y entrega tokens nuevos.
    public AuthResponse refrescar(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.leerClaims(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Sesión expirada, vuelve a iniciar sesión.");
        }
        if (!"refresh".equals(claims.get("tipo", String.class))) {
            throw new BadCredentialsException("Token no válido para renovar la sesión.");
        }
        Usuario u = usuarioRepository.findByCorreoIgnoreCase(claims.getSubject())
                .orElseThrow(() -> new BadCredentialsException("El usuario ya no existe."));
        return crearRespuesta(u);
    }

    // Datos del usuario con sesión iniciada.
    public UsuarioResponse perfil(String correo) {
        Usuario u = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        return usuarioService.aRespuesta(u);
    }

    private AuthResponse crearRespuesta(Usuario u) {
        return new AuthResponse(
                jwtService.generarToken(u),
                jwtService.generarRefreshToken(u),
                "Bearer",
                jwtService.getExpiracionMs(),
                usuarioService.aRespuesta(u));
    }
}