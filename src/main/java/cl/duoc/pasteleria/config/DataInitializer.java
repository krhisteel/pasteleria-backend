package cl.duoc.pasteleria.config;

import cl.duoc.pasteleria.entity.Rol;
import cl.duoc.pasteleria.entity.Usuario;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Crea las 3 cuentas demo al iniciar la aplicación (solo si no existen).
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crearSiNoExiste("Administrador", "admin@duoc.cl", "admin123", Rol.ADMIN);
        crearSiNoExiste("Vendedor", "vendedor@duoc.cl", "venta123", Rol.VENDEDOR);
        crearSiNoExiste("Cliente", "cliente@gmail.com", "cliente1", Rol.CLIENTE);
    }

    private void crearSiNoExiste(String nombre, String correo, String password, Rol rol) {
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) return;

        usuarioRepository.save(Usuario.builder()
                .nombre(nombre)
                .apellidos("Demo")
                .correo(correo)
                .password(passwordEncoder.encode(password))
                .rol(rol)
                .region("Región Metropolitana de Santiago")
                .comuna("Santiago")
                .direccion("Dirección demo 123")
                .build());
    }
}