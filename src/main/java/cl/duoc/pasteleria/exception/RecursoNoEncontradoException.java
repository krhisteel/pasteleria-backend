package cl.duoc.pasteleria.exception;

// Error 404: no existe el registro buscado.
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}