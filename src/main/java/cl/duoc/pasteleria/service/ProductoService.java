
package cl.duoc.pasteleria.service;

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

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un producto con ID: " + id
                ));
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoria(Long categoriaId) {
        return productoRepository
                .findByCategoriaIdAndActivoTrue(categoriaId);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository
                .findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional
    public Producto crear(Producto producto) {

        validarProducto(producto);

        Categoria categoria = obtenerCategoria(
                producto.getCategoria().getId()
        );

        producto.setId(null);
        producto.setNombre(producto.getNombre().trim());
        producto.setCategoria(categoria);
        producto.setActivo(true);

        if (producto.getCodigo() != null) {
            producto.setCodigo(producto.getCodigo().trim());
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos) {

        Producto producto = buscarPorId(id);

        validarProducto(datos);

        Categoria categoria = obtenerCategoria(
                datos.getCategoria().getId()
        );

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

        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {

        Producto producto = buscarPorId(id);

        // Eliminación lógica: conserva el registro
        // para no perder referencias de compras.
        producto.setActivo(false);

        productoRepository.save(producto);
    }

    private Categoria obtenerCategoria(Long id) {

        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una categoría con ID: " + id
                ));
    }

    private void validarProducto(Producto producto) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "Los datos del producto son obligatorios"
            );
        }

        // Validar nombre
        if (producto.getNombre() == null
                || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio"
            );
        }

        if (producto.getNombre().trim().length() > 150) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 150 caracteres"
            );
        }

        // Validar precio
        if (producto.getPrecio() == null
                || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero"
            );
        }

        // Validar stock
        if (producto.getStock() == null
                || producto.getStock() < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo"
            );
        }

        // Validar categoría
        if (producto.getCategoria() == null
                || producto.getCategoria().getId() == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar una categoría"
            );
        }

        // Validar código
        if (producto.getCodigo() != null
                && producto.getCodigo().trim().length() > 20) {
            throw new IllegalArgumentException(
                    "El código no puede superar los 20 caracteres"
            );
        }

        // Validar stock crítico
        if (producto.getStockCritico() != null
                && producto.getStockCritico() < 0) {
            throw new IllegalArgumentException(
                    "El stock crítico no puede ser negativo"
            );
        }

        // Validar descuento (porcentaje entre 0 y 100)
        if (producto.getDescuento() != null
                && (producto.getDescuento().compareTo(BigDecimal.ZERO) < 0
                || producto.getDescuento().compareTo(
                new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException(
                    "El descuento debe estar entre 0 y 100"
            );
        }
    }
}
