package cl.duoc.pasteleria.repository;

import cl.duoc.pasteleria.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring crea automáticamente las consultas a la base de datos a partir del nombre del método.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    Optional<Usuario> findByRun(String run);
    boolean existsByCorreoIgnoreCase(String correo);
}