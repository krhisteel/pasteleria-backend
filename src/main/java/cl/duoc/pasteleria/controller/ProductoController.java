
package cl.duoc.pasteleria.controller;

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
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(
                productoService.listarActivos()
        );
    }

    // Obtener un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productoService.buscarPorId(id)
        );
    }

    // Buscar productos por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscar(
            @RequestParam String nombre) {

        return ResponseEntity.ok(
                productoService.buscarPorNombre(nombre)
        );
    }

    // Filtrar productos activos por categoría
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> porCategoria(
            @PathVariable Long categoriaId) {

        return ResponseEntity.ok(
                productoService.buscarPorCategoria(categoriaId)
        );
    }

    // Crear un producto
    @PostMapping
    public ResponseEntity<Producto> crear(
            @Valid @RequestBody Producto producto) {

        Producto nuevo = productoService.crear(producto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevo);
    }

    // Actualizar un producto
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Producto producto) {

        return ResponseEntity.ok(
                productoService.actualizar(id, producto)
        );
    }

    // Desactivar un producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        productoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
