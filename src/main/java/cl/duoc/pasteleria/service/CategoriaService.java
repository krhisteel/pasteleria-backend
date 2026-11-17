
package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.entity.Categoria;
import cl.duoc.pasteleria.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe una categoría con ID: " + id
                        )
                );
    }

    @Transactional
    public Categoria crear(Categoria categoria) {

        String nombre = validarNombre(categoria.getNombre());

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría con ese nombre"
            );
        }

        categoria.setId(null);
        categoria.setNombre(nombre);

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Long id, Categoria datos) {

        Categoria categoria = buscarPorId(id);
        String nombre = validarNombre(datos.getNombre());

        categoriaRepository.findByNombreIgnoreCase(nombre)
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Ya existe una categoría con ese nombre"
                        );
                    }
                });

        categoria.setNombre(nombre);
        categoria.setDescripcion(datos.getDescripcion());

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {

        Categoria categoria = buscarPorId(id);

        categoriaRepository.delete(categoria);
    }

    private String validarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio"
            );
        }

        String limpio = nombre.trim();

        if (limpio.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres"
            );
        }

        return limpio;
    }
}
