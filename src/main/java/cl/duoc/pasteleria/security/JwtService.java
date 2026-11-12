package cl.duoc.pasteleria.security;

import cl.duoc.pasteleria.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

// Genera y verifica los tokens JWT (firmados con HMAC-SHA).
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;
    private final long refreshExpiracionMs;

    // Lee la clave secreta y los tiempos de expiración desde application.properties.
    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiration-ms}") long expiracionMs,
                      @Value("${jwt.refresh-expiration-ms}") long refreshExpiracionMs) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        this.expiracionMs = expiracionMs;
        this.refreshExpiracionMs = refreshExpiracionMs;
    }

    // Token corto (1 hora) para llamar a la API.
    public String generarToken(Usuario usuario) {
        return construir(usuario, "access", expiracionMs);
    }

    // Token largo (7 días) que solo sirve para pedir un token nuevo.
    public String generarRefreshToken(Usuario usuario) {
        return construir(usuario, "refresh", refreshExpiracionMs);
    }

    private String construir(Usuario usuario, String tipo, long duracionMs) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .claim("tipo", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave)
                .compact();
    }

    // Verifica firma y expiración. Lanza JwtException si algo está mal.
    public Claims leerClaims(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }

    // true solo si la firma es válida, no expiró y es un token de acceso (no refresh).
    public boolean esAccessTokenValido(String token) {
        try {
            return "access".equals(leerClaims(token).get("tipo", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }
}