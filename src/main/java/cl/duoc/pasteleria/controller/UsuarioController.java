package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.dto.UsuarioRequest;
import cl.duoc.pasteleria.dto.UsuarioResponse;
import cl.duoc.pasteleria.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CRUD de usuarios. Doble protección: regla en SecurityConfig + @PreAuthorize.
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Gestión de usuarios (solo Administrador)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Lista todos los usuarios")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario por id")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario con cualquier rol")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest req) {
        return usuarioService.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un usuario")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody UsuarioRequest req,
                                      Authentication auth) {
        return usuarioService.actualizar(id, req, auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un usuario")
    public void eliminar(@PathVariable Long id, Authentication auth) {
        usuarioService.eliminar(id, auth.getName());
    }
}