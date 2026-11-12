package cl.duoc.pasteleria.entity;

import cl.duoc.pasteleria.exception.ReglaNegocioException;

// Roles del sistema. "clave" y "nombre" son los textos que usa el frontend.
public enum Rol {
    ADMIN("admin", "Administrador"),
    VENDEDOR("vendedor", "Vendedor"),
    CLIENTE("cliente", "Cliente");

    private final String clave;
    private final String nombre;

    Rol(String clave, String nombre) {
        this.clave = clave;
        this.nombre = nombre;
    }

    public String getClave() { return clave; }
    public String getNombre() { return nombre; }

    // Convierte "admin", "administrador", "vendedor" o "cliente" al enum.
    public static Rol desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El rol es obligatorio.");
        }
        return switch (texto.trim().toLowerCase()) {
            case "admin", "administrador" -> ADMIN;
            case "vendedor" -> VENDEDOR;
            case "cliente" -> CLIENTE;
            default -> throw new ReglaNegocioException("Rol no válido: " + texto);
        };
    }
}