package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.security.*;
import cl.duoc.pasteleria.service.*;
import cl.duoc.pasteleria.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;

@WebMvcTest({ProductoController.class, CategoriaController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class CatalogoSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean ProductoService productoService;
    @MockitoBean CategoriaService categoriaService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UsuarioDetailsService usuarioDetailsService;
    private static final String PRODUCTO = """
        {"codigo":"TC003","nombre":"Torta","descripcion":"Chocolate","precio":100,
         "stock":3,"stockCritico":1,"descuento":0,"imagenUrl":"/images/torta.jpg","categoria":{"id":8}}
        """;
    @Test void consultasPublicasSinSesion() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/categorias")).andExpect(status().isOk());
    }
    @Test void adminListadoYEscriturasRequierenSesion() throws Exception {
        mvc.perform(get("/api/v1/productos/admin")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)).andExpect(status().isUnauthorized());
    }
    @Test @WithMockUser(roles="CLIENTE") void clienteNoVeListadoAdministrativo() throws Exception {
        mvc.perform(get("/api/v1/productos/admin")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(roles="VENDEDOR") void vendedorConsultaPeroNoEscribe() throws Exception {
        mvc.perform(get("/api/v1/productos/admin")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/productos/17").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/productos/17")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Tortas\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/categorias/8").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Tortas\"}")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/categorias/8")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(roles="ADMIN") void adminPuedeOperar() throws Exception {
        mvc.perform(get("/api/v1/productos/admin")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)).andExpect(status().isCreated());
        mvc.perform(put("/api/v1/productos/17").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/productos/17")).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Tortas\"}")).andExpect(status().isCreated());
        mvc.perform(put("/api/v1/categorias/8").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Tortas\"}")).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/categorias/8")).andExpect(status().isNoContent());
    }
    @Test @WithMockUser(roles="ADMIN") void erroresTienenCodigosHTTP() throws Exception {
        doThrow(new ConflictoException("Tiene productos inactivos")).when(categoriaService).eliminar(8L);
        mvc.perform(delete("/api/v1/categorias/8")).andExpect(status().isConflict()).andExpect(jsonPath("$.mensaje").value("Tiene productos inactivos"));
        when(productoService.buscarPorId(17L)).thenThrow(new RecursoNoEncontradoException("Producto no encontrado"));
        mvc.perform(get("/api/v1/productos/17")).andExpect(status().isNotFound());
        when(productoService.crear(any())).thenThrow(new ReglaNegocioException("Codigo inmutable"));
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)).andExpect(status().isBadRequest());
        doThrow(new DataIntegrityViolationException("duplicate")).when(productoService).crear(any());
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO)).andExpect(status().isConflict());
    }
    @Test @WithMockUser(roles="ADMIN") void validacionBeanEs400() throws Exception {
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content("{\"precio\":0}")).andExpect(status().isBadRequest());
        verifyNoInteractions(productoService);
    }
}
