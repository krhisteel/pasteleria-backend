package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.security.JwtAuthFilter;
import cl.duoc.pasteleria.security.JwtService;
import cl.duoc.pasteleria.security.SecurityConfig;
import cl.duoc.pasteleria.security.UsuarioDetailsService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Slice MVC sin JPA, repositorios, servicios de negocio ni inicializadores.
@WebMvcTest(controllers = BoletasSecurityTest.BoletasSimuladas.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, BoletasSecurityTest.BoletasSimuladas.class})
class BoletasSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean UsuarioDetailsService usuarioDetailsService;

    static Stream<Arguments> permisos() {
        // La excepción mis-boletas debe conservarse antes del GET administrativo.
        String[][] operaciones = {
                {"GET", "", "ADMIN,VENDEDOR"},
                {"GET", "/17", "ADMIN,VENDEDOR"},
                {"GET", "/mis-boletas", "ADMIN,VENDEDOR,CLIENTE"},
                {"POST", "", "ADMIN,CLIENTE"},
                {"PUT", "/17", "ADMIN"},
                {"PATCH", "/17", "ADMIN"},
                {"DELETE", "/17", "ADMIN"},
                // Un POST fuera de la creación exacta no debe heredar su permiso.
                {"POST", "/17", "ADMIN"}
        };
        return List.of("ADMIN", "VENDEDOR", "CLIENTE", "SIN_SESION").stream()
                .flatMap(rol -> Arrays.stream(operaciones).map(operacion -> {
                    boolean permitido = Arrays.asList(operacion[2].split(",")).contains(rol);
                    int estado = permitido ? 200 : rol.equals("SIN_SESION") ? 401 : 403;
                    return Arguments.of(rol, operacion[0], "/api/v1/boletas" + operacion[1], estado);
                }));
    }

    @ParameterizedTest(name = "{0}: {1} {2} -> {3}")
    @MethodSource("permisos")
    void aplicaReglasSinAccederABaseDeDatos(String rol, String metodo, String ruta, int estado)
            throws Exception {
        MockHttpServletRequestBuilder solicitud = request(HttpMethod.valueOf(metodo), ruta);
        if (!rol.equals("SIN_SESION")) {
            solicitud.with(user("usuario-prueba").roles(rol));
        }
        var resultado = mvc.perform(solicitud).andExpect(status().is(estado));
        if (estado == 200) {
            resultado.andExpect(content().string("autorizado-solo-en-test"));
        } else {
            resultado.andExpect(jsonPath("$.status").value(estado));
        }
    }

    // Exclusivo de src/test: confirma que la petición atravesó la cadena real de
    // seguridad. El 200 no representa un contrato HTTP ni un CRUD implementado.
    @RestController
    @RequestMapping("/api/v1/boletas")
    public static class BoletasSimuladas {
        @RequestMapping({"", "/{segmento}"})
        String responder() {
            return "autorizado-solo-en-test";
        }
    }
}
