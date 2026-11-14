
package cl.duoc.pasteleria.repository;

import cl.duoc.pasteleria.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    boolean existsByCategoriaId(Long categoriaId);
}
