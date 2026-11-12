package cl.duoc.pasteleria.security;

import cl.duoc.pasteleria.entity.Rol;
import cl.duoc.pasteleria.entity.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRETO = "cGFzdGVsZXJpYS0xMDAwLXNhYm9yZXMtY2xhdmUtc2VjcmV0YS1qd3QtMjAyNi1kdW9j";
    private final JwtService jwtService = new JwtService(SECRETO, 3_600_000, 604_800_000);

    private Usuario admin() {
        return Usuario.builder().id(1L).correo("admin@duoc.cl").rol(Rol.ADMIN).build();
    }

    @Test
    void generaTokenValidoConCorreoYRol() {
        String token = jwtService.generarToken(admin());

        assertTrue(jwtService.esAccessTokenValido(token));
        assertEquals("admin@duoc.cl", jwtService.leerClaims(token).getSubject());
        assertEquals("ADMIN", jwtService.leerClaims(token).get("rol"));
    }

    @Test
    void refreshTokenNoSirveComoTokenDeAcceso() {
        assertFalse(jwtService.esAccessTokenValido(jwtService.generarRefreshToken(admin())));
    }

    @Test
    void tokenFalsoEsRechazado() {
        assertFalse(jwtService.esAccessTokenValido("abc.def.ghi"));
    }

    @Test
    void tokenExpiradoEsRechazado() {
        JwtService vencido = new JwtService(SECRETO, -1000, -1000);
        assertFalse(jwtService.esAccessTokenValido(vencido.generarToken(admin())));
    }
}