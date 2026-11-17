package cl.duoc.pasteleria.config;

import cl.duoc.pasteleria.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// ApplicationContextRunner no ejecuta CommandLineRunner ni arranca servidores.
// No se importa la aplicacion principal, JPA ni un DataSource.
class DataInitializerConfigurationTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Contexto.class)
            .withBean(UsuarioRepository.class, () -> usuarios)
            .withBean(PasswordEncoder.class, () -> encoder);

    @Configuration(proxyBeanMethods = false)
    @Import(DataInitializer.class)
    static class Contexto {}

    @Test
    void sinPropiedadConservaRegistroNormalSinEjecutarInicializador() {
        runner.run(contexto -> {
            assertThat(contexto).hasNotFailed().hasSingleBean(DataInitializer.class);
            verifyNoInteractions(usuarios, encoder);
        });
    }

    @Test
    void exclusionDesactivadaConservaRegistroNormalSinEjecutarlo() {
        runner.withPropertyValues("app.data-initializer.disabled=false").run(contexto -> {
            assertThat(contexto).hasNotFailed().hasSingleBean(DataInitializer.class);
            verifyNoInteractions(usuarios, encoder);
        });
    }

    @Test
    void exclusionExplicitaEvitaBeanYCualquierAccesoADatos() {
        runner.withPropertyValues("app.data-initializer.disabled=true").run(contexto -> {
            assertThat(contexto).hasNotFailed().doesNotHaveBean(DataInitializer.class);
            verifyNoInteractions(usuarios, encoder);
        });
    }

    @Test
    void contextoAisladoPuedeExcluirloInclusoSinRepositoriosNiEncoder() {
        new ApplicationContextRunner().withUserConfiguration(Contexto.class)
                .withPropertyValues("app.data-initializer.disabled=true")
                .run(contexto -> assertThat(contexto).hasNotFailed().doesNotHaveBean(DataInitializer.class));
    }
}
