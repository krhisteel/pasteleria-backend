package cl.duoc.pasteleria;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import cl.duoc.pasteleria.repository.ProductoRepository;
import cl.duoc.pasteleria.repository.CategoriaRepository;
import cl.duoc.pasteleria.service.ProductoService;
import cl.duoc.pasteleria.service.CategoriaService;

// Contexto aislado: no carga JPA, MySQL ni DataInitializer.
@SpringBootTest(classes = PasteleriaApplicationTests.ContextoAislado.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PasteleriaApplicationTests {
    @Configuration(proxyBeanMethods = false)
    @Import({ProductoService.class, CategoriaService.class})
    static class ContextoAislado {}

    @MockitoBean ProductoRepository productoRepository;
    @MockitoBean CategoriaRepository categoriaRepository;

	@Test
	void contextLoads() {
	}

}
