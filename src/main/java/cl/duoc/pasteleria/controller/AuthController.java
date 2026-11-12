package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.dto.*;
import cl.duoc.pasteleria.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login, registro, renovación de token y perfil")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión", description = "Devuelve un token JWT (1 h), un refresh token (7 días) y los datos del usuario.")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un cliente nuevo")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return authService.registrar(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renueva el token", description = "Recibe el refresh token y devuelve tokens nuevos.")
    public AuthResponse refrescar(@Valid @RequestBody RefreshRequest req) {
        return authService.refrescar(req.refreshToken());
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Perfil del usuario con sesión iniciada")
    public UsuarioResponse perfil(Authentication auth) {
        return authService.perfil(auth.getName());
    }
}