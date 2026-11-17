
package cl.duoc.pasteleria.controller;

import cl.duoc.pasteleria.dto.ProductoResponseDTO;
import cl.duoc.pasteleria.entity.Producto;
import cl.duoc.pasteleria.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // Listar productos activos para el catálogo
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listar() {
        return ResponseEntity.ok(productoService.listarActivos());
    }

    // Buscar un producto por su ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productoService.buscarPorId(id)
        );
    }

    // Buscar productos por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorNombre(
            @RequestParam String nombre) {

        return ResponseEntity.ok(
                productoService.buscarPorNombre(nombre)
        );
    }

    // Listar productos activos de una categoría
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorCategoria(
            @PathVariable Long categoriaId) {

        return ResponseEntity.ok(
                productoService.buscarPorCategoria(categoriaId)
        );
    }

    // Crear producto
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(
            @Valid @RequestBody Producto producto) {

        ProductoResponseDTO creado = productoService.crear(producto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    // Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Producto producto) {

        return ResponseEntity.ok(
                productoService.actualizar(id, producto)
        );
    }

    // Eliminación lógica del producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        productoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
