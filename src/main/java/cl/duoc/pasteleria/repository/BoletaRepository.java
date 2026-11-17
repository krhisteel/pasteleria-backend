package cl.duoc.pasteleria.repository;

import cl.duoc.pasteleria.entity.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {
    // Boletas de un cliente, de la más nueva a la más antigua.
    List<Boleta> findByUsuarioCorreoIgnoreCaseOrderByFechaDesc(String correo);

    // Todas las boletas (ADMIN y VENDEDOR), de la más nueva a la más antigua.
    List<Boleta> findAllByOrderByFechaDesc();
}