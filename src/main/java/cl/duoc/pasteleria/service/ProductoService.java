
package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.ProductoResponseDTO;
import cl.duoc.pasteleria.entity.Categoria;
import cl.duoc.pasteleria.entity.Producto;
import cl.duoc.pasteleria.repository.CategoriaRepository;
import cl.duoc.pasteleria.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // Listar todos los productos, incluidos los inactivos
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listar() {
        return productoRepository.findAll()
                .stream()
                .map(ProductoResponseDTO::desdeEntidad)
                .toList();
    }

    // Listar solamente productos activos
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarActivos() {
        return productoRepository.findByActivoTrue()
                .stream()
                .map(ProductoResponseDTO::desdeEntidad)
                .toList();
    }

    // Buscar producto por ID
    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorId(Long id) {
        Producto producto = obtenerProducto(id);
        return ProductoResponseDTO.desdeEntidad(producto);
    }

    // Buscar productos activos por categoría
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaIdAndActivoTrue(categoriaId)
                .stream()
                .map(ProductoResponseDTO::desdeEntidad)
                .toList();
    }

    // Buscar productos por nombre
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(ProductoResponseDTO::desdeEntidad)
                .toList();
    }

    // Crear un producto
    @Transactional
    public ProductoResponseDTO crear(Producto producto) {
        validarProducto(producto);

        Categoria categoria = obtenerCategoria(producto.getCategoria().getId());

        producto.setId(null);
        producto.setNombre(producto.getNombre().trim());
        producto.setCategoria(categoria);
        producto.setActivo(true);

        if (producto.getCodigo() != null) {
            producto.setCodigo(producto.getCodigo().trim());
        }

        Producto guardado = productoRepository.save(producto);
        return ProductoResponseDTO.desdeEntidad(guardado);
    }

    // Actualizar un producto existente
    @Transactional
    public ProductoResponseDTO actualizar(Long id, Producto datos) {
        Producto producto = obtenerProducto(id);
        validarProducto(datos);

        Categoria categoria = obtenerCategoria(datos.getCategoria().getId());

        producto.setNombre(datos.getNombre().trim());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());
        producto.setStockCritico(datos.getStockCritico());
        producto.setCodigo(
                datos.getCodigo() != null
                        ? datos.getCodigo().trim()
                        : null
        );
        producto.setDescuento(datos.getDescuento());
        producto.setImagenUrl(datos.getImagenUrl());
        producto.setCategoria(categoria);

        if (datos.getActivo() != null) {
            producto.setActivo(datos.getActivo());
        }

        Producto actualizado = productoRepository.save(producto);
        return ProductoResponseDTO.desdeEntidad(actualizado);
    }

    // Eliminación lógica: no borra el registro de MySQL
    @Transactional
    public void eliminar(Long id) {
        Producto producto = obtenerProducto(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    // Obtener producto o lanzar error
    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe un producto con ID: " + id
                        )
                );
    }

    // Obtener categoría o lanzar error
    private Categoria obtenerCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe una categoría con ID: " + id
                        )
                );
    }

    // Validaciones de producto
    private void validarProducto(Producto producto) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "Los datos del producto son obligatorios"
            );
        }

        if (producto.getNombre() == null ||
                producto.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio"
            );
        }

        if (producto.getNombre().trim().length() > 150) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 150 caracteres"
            );
        }

        if (producto.getPrecio() == null ||
                producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero"
            );
        }

        if (producto.getStock() == null ||
                producto.getStock() < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo"
            );
        }

        if (producto.getCategoria() == null ||
                producto.getCategoria().getId() == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar una categoría"
            );
        }

        if (producto.getCodigo() != null &&
                producto.getCodigo().trim().length() > 20) {
            throw new IllegalArgumentException(
                    "El código no puede superar los 20 caracteres"
            );
        }

        if (producto.getStockCritico() != null &&
                producto.getStockCritico() < 0) {
            throw new IllegalArgumentException(
                    "El stock crítico no puede ser negativo"
            );
        }

        if (producto.getDescuento() != null &&
                (producto.getDescuento().compareTo(BigDecimal.ZERO) < 0 ||
                        producto.getDescuento().compareTo(
                                new BigDecimal("100")
                        ) > 0)) {
            throw new IllegalArgumentException(
                    "El descuento debe estar entre 0 y 100"
            );
        }
    }
}
