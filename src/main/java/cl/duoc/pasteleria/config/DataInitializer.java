package cl.duoc.pasteleria.config;

import cl.duoc.pasteleria.entity.Rol;
import cl.duoc.pasteleria.entity.Usuario;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// Crea las 3 cuentas demo al iniciar la aplicación (solo si no existen).
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.admin-password:}")
    private String adminPassword;

    @Value("${app.demo.vendedor-password:}")
    private String vendedorPassword;

    @Value("${app.demo.cliente-password:}")
    private String clientePassword;

    private record CuentaDemo(String nombre, String correo, String password, Rol rol, String variable) {}

    @Override
    public void run(String... args) {
        List<CuentaDemo> pendientes = List.of(
                new CuentaDemo("Administrador", "admin@duoc.cl", adminPassword, Rol.ADMIN, "DEMO_ADMIN_PASSWORD"),
                new CuentaDemo("Vendedor", "vendedor@duoc.cl", vendedorPassword, Rol.VENDEDOR, "DEMO_VENDEDOR_PASSWORD"),
                new CuentaDemo("Cliente", "cliente@gmail.com", clientePassword, Rol.CLIENTE, "DEMO_CLIENTE_PASSWORD")
        ).stream().filter(cuenta -> !usuarioRepository.existsByCorreoIgnoreCase(cuenta.correo())).toList();

        // Validar todas las cuentas faltantes antes de insertar; no imprimir sus claves.
        for (CuentaDemo cuenta : pendientes) {
            String password = cuenta.password();
            if (password == null || password.isBlank() || password.length() < 4 || password.length() > 10) {
                throw new IllegalStateException("Configure " + cuenta.variable()
                        + " con una contraseña de 4 a 10 caracteres para crear la cuenta demo faltante.");
            }
        }
        for (CuentaDemo cuenta : pendientes) {
            crear(cuenta.nombre(), cuenta.correo(), cuenta.password(), cuenta.rol());
        }
    }

    private void crear(String nombre, String correo, String password, Rol rol) {
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
