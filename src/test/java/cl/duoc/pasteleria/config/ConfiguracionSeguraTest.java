package cl.duoc.pasteleria.config;

import cl.duoc.pasteleria.entity.Rol;
import cl.duoc.pasteleria.entity.Usuario;
import cl.duoc.pasteleria.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

// Solo resolución de propiedades y JWT en memoria. Sin contexto JPA ni conexión JDBC.
class ConfiguracionSeguraTest {
    private final Properties propiedades = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));

    ConfiguracionSeguraTest() throws IOException {}

    @Test
    void jwtRequiereConfiguracionExternaSinClaveDeRespaldo() {
        assertEquals("${JWT_SECRET}", propiedades.getProperty("jwt.secret"));
        assertThrows(IllegalArgumentException.class, () -> new MockEnvironment()
                .resolveRequiredPlaceholders(propiedades.getProperty("jwt.secret")));
    }

    @Test
    void jwtConfiguradoGeneraYVerificaTokens() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        var entorno = new MockEnvironment().withProperty("JWT_SECRET", Base64.getEncoder().encodeToString(bytes));
        var jwt = new JwtService(entorno.resolveRequiredPlaceholders(propiedades.getProperty("jwt.secret")), 60000, 120000);
        var usuario = Usuario.builder().correo("qa@example.test").rol(Rol.ADMIN).build();
        assertTrue(jwt.esAccessTokenValido(jwt.generarToken(usuario)));
        assertFalse(jwt.esAccessTokenValido(jwt.generarRefreshToken(usuario)));
    }

    @Test
    void destinoYCredencialesNoTienenValoresPredeterminados() {
        assertEquals("${DB_URL}", propiedades.getProperty("spring.datasource.url"));
        assertEquals("${DB_USER}", propiedades.getProperty("spring.datasource.username"));
        assertEquals("${DB_PASSWORD}", propiedades.getProperty("spring.datasource.password"));
        for (String key : new String[]{"spring.datasource.url", "spring.datasource.username", "spring.datasource.password"}) {
            assertThrows(IllegalArgumentException.class, () -> new MockEnvironment()
                    .resolveRequiredPlaceholders(propiedades.getProperty(key)));
        }
    }

    @Test
    void aceptaMariaDBEnCualquierPuertoSinCrearUnaBaseAutomaticamente() {
        String destino = "jdbc:mysql://127.0.0.1:3399/qa_configuracion";
        var entorno = new MockEnvironment().withProperty("DB_URL", destino);
        assertEquals(destino, entorno.resolveRequiredPlaceholders(propiedades.getProperty("spring.datasource.url")));
        assertFalse(propiedades.getProperty("spring.datasource.url").contains("createDatabaseIfNotExist"));
    }

    @Test
    void arranqueNormalValidaYPreparacionSeSolicitaExplicitamente() {
        String ddl = propiedades.getProperty("spring.jpa.hibernate.ddl-auto");
        assertEquals("validate", new MockEnvironment().resolveRequiredPlaceholders(ddl));
        assertEquals("create-only", new MockEnvironment().withProperty("JPA_DDL_AUTO", "create-only").resolveRequiredPlaceholders(ddl));
        assertEquals("never", propiedades.getProperty("spring.sql.init.mode"));
    }
}
