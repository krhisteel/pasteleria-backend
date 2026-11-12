package cl.duoc.pasteleria.dto;

import jakarta.validation.constraints.NotBlank;

// Datos para renovar el token: solo el refresh token.
public record RefreshRequest(@NotBlank String refreshToken) {}