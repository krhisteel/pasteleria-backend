package cl.duoc.pasteleria.exception;

// Error 400: la petición no cumple una regla de negocio.
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}