package cl.duoc.pasteleria.config;

import cl.duoc.pasteleria.entity.Rol;
import cl.duoc.pasteleria.entity.Usuario;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DataInitializerTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(SoloInicializador.class)
            .withBean(UsuarioRepository.class, () -> usuarios)
            .withBean(PasswordEncoder.class, () -> encoder)
            .withPropertyValues("DEMO_ADMIN_PASSWORD=", "DEMO_VENDEDOR_PASSWORD=", "DEMO_CLIENTE_PASSWORD=");

    @Configuration(proxyBeanMethods = false)
    @Import(DataInitializer.class)
    static class SoloInicializador {}

    private String claveDePrueba() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    @Test
    void creaTresRolesConCredencialesExternasYBCrypt() {
        String admin = claveDePrueba(), vendedor = claveDePrueba(), cliente = claveDePrueba();
        contexto.withPropertyValues("DEMO_ADMIN_PASSWORD=" + admin,
                "DEMO_VENDEDOR_PASSWORD=" + vendedor, "DEMO_CLIENTE_PASSWORD=" + cliente).run(ctx -> {
            assertNull(ctx.getStartupFailure());
            assertTrue(ctx.getBeansOfType(DataSource.class).isEmpty());
            ctx.getBean(DataInitializer.class).run();
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarios, times(3)).save(captor.capture());
            List<Usuario> creados = captor.getAllValues();
            assertEquals(List.of(Rol.ADMIN, Rol.VENDEDOR, Rol.CLIENTE), creados.stream().map(Usuario::getRol).toList());
            assertTrue(encoder.matches(admin, creados.get(0).getPassword()));
            assertTrue(encoder.matches(vendedor, creados.get(1).getPassword()));
            assertTrue(encoder.matches(cliente, creados.get(2).getPassword()));
        });
    }

    @Test
    void conservaUsuariosExistentesSinPedirNuevasCredenciales() {
        when(usuarios.existsByCorreoIgnoreCase(anyString())).thenReturn(true);
        contexto.run(ctx -> {
            assertNull(ctx.getStartupFailure());
            ctx.getBean(DataInitializer.class).run();
            verify(usuarios, never()).save(any());
        });
    }

    @Test
    void faltaUnaClaveNoInsertaNingunaCuentaParcial() {
        contexto.withPropertyValues("DEMO_ADMIN_PASSWORD=" + claveDePrueba(),
                "DEMO_VENDEDOR_PASSWORD=" + claveDePrueba()).run(ctx -> {
            var error = assertThrows(IllegalStateException.class, () -> ctx.getBean(DataInitializer.class).run());
            assertTrue(error.getMessage().contains("DEMO_CLIENTE_PASSWORD"));
            verify(usuarios, never()).save(any());
        });
    }

    @Test
    void soloNecesitaLaClaveDeLaCuentaFaltante() {
        when(usuarios.existsByCorreoIgnoreCase(anyString())).thenReturn(true);
        when(usuarios.existsByCorreoIgnoreCase("vendedor@duoc.cl")).thenReturn(false);
        String clave = claveDePrueba();
        contexto.withPropertyValues("DEMO_VENDEDOR_PASSWORD=" + clave).run(ctx -> {
            ctx.getBean(DataInitializer.class).run();
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarios).save(captor.capture());
            assertEquals(Rol.VENDEDOR, captor.getValue().getRol());
            assertTrue(encoder.matches(clave, captor.getValue().getPassword()));
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3, 11})
    void rechazaLongitudesIncompatiblesConElLoginSinEscribir(int longitud) {
        String clave = UUID.randomUUID().toString().replace("-", "").substring(0, longitud);
        contexto.withPropertyValues("DEMO_ADMIN_PASSWORD=" + clave).run(ctx -> {
            assertThrows(IllegalStateException.class, () -> ctx.getBean(DataInitializer.class).run());
            verify(usuarios, never()).save(any());
        });
    }
}
