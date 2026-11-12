package cl.duoc.pasteleria.exception;

// Error 409: el dato ya existe (correo o RUN repetido).
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}