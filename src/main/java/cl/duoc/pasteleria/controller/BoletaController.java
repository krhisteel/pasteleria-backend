package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.dto.BoletaResponse;
import cl.duoc.pasteleria.dto.CompraRequest;
import cl.duoc.pasteleria.dto.EstadoBoletaRequest;
import cl.duoc.pasteleria.service.BoletaService;
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

@RestController
@RequestMapping("/api/v1/boletas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Boletas", description = "Compras y boletas")
public class BoletaController {

    private final BoletaService boletaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Confirma una compra",
            description = "Valida el stock, calcula precios y descuentos en el servidor, descuenta el stock y guarda la boleta.")
    public BoletaResponse comprar(@Valid @RequestBody CompraRequest req, Authentication auth) {
        return boletaService.comprar(auth.getName(), req);
    }

    @GetMapping("/mis-boletas")
    @Operation(summary = "Boletas del usuario con sesión")
    public List<BoletaResponse> misBoletas(Authentication auth) {
        return boletaService.misBoletas(auth.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(summary = "Lista todas las boletas (ADMIN y VENDEDOR)")
    public List<BoletaResponse> listar() {
        return boletaService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(summary = "Obtiene una boleta con su detalle (ADMIN y VENDEDOR)")
    public BoletaResponse obtener(@PathVariable Long id) {
        return boletaService.obtener(id);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualiza el estado de una boleta (solo ADMIN)",
            description = "Cambia el estado de la orden. Si pasa a 'cancelado', restituye el stock al inventario.")
    public BoletaResponse actualizarEstado(@PathVariable Long id,
                                           @Valid @RequestBody EstadoBoletaRequest req) {
        return boletaService.actualizarEstado(id, req.estado());
    }
}