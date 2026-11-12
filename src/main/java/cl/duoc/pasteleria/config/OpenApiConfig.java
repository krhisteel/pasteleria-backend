package cl.duoc.pasteleria.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Información general de la API y botón "Authorize" en Swagger para pegar el token JWT.
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API Pastelería 1000 Sabores",
        version = "v1",
        description = "Backend de la tienda: usuarios, productos, categorías y boletas."))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}