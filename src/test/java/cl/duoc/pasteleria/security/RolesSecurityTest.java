package cl.duoc.pasteleria.security;

import cl.duoc.pasteleria.controller.*;
import cl.duoc.pasteleria.service.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.stream.Stream;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Solo MVC y seguridad reales: todos los servicios de negocio son mocks, sin JPA ni DataInitializer.
@WebMvcTest(controllers = {BoletaController.class, ProductoController.class,
        CategoriaController.class, UsuarioController.class}, properties = {
        "app.cors.origins=http://localhost:5173",
        "spring.sql.init.mode=never",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class RolesSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean BoletaService boletas;
    @MockitoBean ProductoService productos;
    @MockitoBean CategoriaService categorias;
    @MockitoBean UsuarioService usuarios;
    @MockitoBean JwtService jwt;
    @MockitoBean UsuarioDetailsService detalles;

    record Caso(String metodo, String ruta, String cuerpo, int exito, String roles) {}

    static Stream<Object[]> permisos() {
        String producto = """
                {"codigo":"QA","nombre":"Torta QA","descripcion":"Prueba","precio":1500,
                 "stock":5,"stockCritico":1,"descuento":0,"categoria":{"id":1}}
                """;
        String compra = """
                {"items":[{"productoId":17,"cantidad":1}],"nombre":"QA",
                 "direccion":"Direccion QA","metodoPago":"transferencia"}
                """;
        return Stream.of(
                new Caso("GET", "/productos", "", 200, "TODOS"),
                new Caso("GET", "/productos/17", "", 200, "TODOS"),
                new Caso("GET", "/categorias", "", 200, "TODOS"),
                new Caso("POST", "/productos", producto, 201, "ADMIN"),
                new Caso("PUT", "/productos/17", producto, 200, "ADMIN"),
                new Caso("DELETE", "/productos/17", "", 204, "ADMIN"),
                new Caso("POST", "/categorias", "{\"nombre\":\"Categoria QA\"}", 201, "ADMIN"),
                new Caso("PUT", "/categorias/1", "{\"nombre\":\"Categoria QA\"}", 200, "ADMIN"),
                new Caso("DELETE", "/categorias/1", "", 204, "ADMIN"),
                new Caso("GET", "/usuarios", "", 200, "ADMIN"),
                new Caso("GET", "/boletas", "", 200, "ADMIN,VENDEDOR"),
                new Caso("GET", "/boletas/17", "", 200, "ADMIN,VENDEDOR"),
                new Caso("GET", "/boletas/mis-boletas", "", 200, "ADMIN,VENDEDOR,CLIENTE"),
                new Caso("POST", "/boletas", compra, 201, "ADMIN,CLIENTE"),
                new Caso("PATCH", "/boletas/17/estado", "{\"estado\":\"procesando\"}", 200, "ADMIN"),
                // No existe controlador para PUT/DELETE de boletas: ADMIN llega al MVC (405).
                new Caso("PUT", "/boletas/17", "{}", 405, "ADMIN"),
                new Caso("DELETE", "/boletas/17", "", 405, "ADMIN")
        ).flatMap(caso -> Stream.of("ADMIN", "VENDEDOR", "CLIENTE", "ANONIMO")
                .map(rol -> new Object[]{caso, rol}));
    }

    @ParameterizedTest(name = "{1}: {0}")
    @MethodSource("permisos")
    void aplicaPermisosSinAccederADatos(Caso caso, String rol) throws Exception {
        boolean permitido = caso.roles().equals("TODOS") || caso.roles().contains(rol);
        int esperado = permitido ? caso.exito() : rol.equals("ANONIMO") ? 401 : 403;
        MockHttpServletRequestBuilder peticion = request(
                org.springframework.http.HttpMethod.valueOf(caso.metodo()), "/api/v1" + caso.ruta())
                .contentType(MediaType.APPLICATION_JSON).content(caso.cuerpo());
        if (!rol.equals("ANONIMO")) peticion.with(user("qa@example.test").roles(rol));
        mvc.perform(peticion).andExpect(status().is(esperado));
        if (!permitido) verifyNoInteractions(boletas, productos, categorias, usuarios);
    }
}
