package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.UsuarioRequest;
import cl.duoc.pasteleria.dto.UsuarioResponse;
import cl.duoc.pasteleria.entity.Rol;
import cl.duoc.pasteleria.entity.Usuario;
import cl.duoc.pasteleria.exception.ConflictoException;
import cl.duoc.pasteleria.exception.RecursoNoEncontradoException;
import cl.duoc.pasteleria.exception.ReglaNegocioException;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    // Cuentas demo: no se pueden borrar ni cambiar su correo/rol.
    public static final List<String> CORREOS_DEMO =
            List.of("admin@duoc.cl", "vendedor@duoc.cl", "cliente@gmail.com");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final BeneficioService beneficioService;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria.");
        }
        String correo = normalizarCorreo(req.correo());
        validarUnicos(correo, normalizarRun(req.run()), null);

        Usuario u = new Usuario();
        copiarDatos(req, u);
        // La contraseña se guarda encriptada con BCrypt.
        u.setPassword(passwordEncoder.encode(req.password()));
        return aRespuesta(usuarioRepository.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest req, String correoSesion) {
        Usuario u = buscar(id);
        String correoNuevo = normalizarCorreo(req.correo());
        Rol rolNuevo = Rol.desdeTexto(req.rol());

        if (CORREOS_DEMO.contains(u.getCorreo())
                && (!u.getCorreo().equals(correoNuevo) || u.getRol() != rolNuevo)) {
            throw new ReglaNegocioException("El correo y el rol de las cuentas demo son fijos.");
        }
        if (u.getCorreo().equalsIgnoreCase(correoSesion) && u.getRol() != rolNuevo) {
            throw new ReglaNegocioException("No puedes cambiar tu propio rol.");
        }

        validarUnicos(correoNuevo, normalizarRun(req.run()), id);
        copiarDatos(req, u);

        // Solo cambia la contraseña si se envió una nueva.
        if (req.password() != null && !req.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.password()));
        }
        return aRespuesta(usuarioRepository.save(u));
    }

    @Transactional
    public void eliminar(Long id, String correoSesion) {
        Usuario u = buscar(id);
        if (CORREOS_DEMO.contains(u.getCorreo())) {
            throw new ReglaNegocioException("Las cuentas demo se conservan para las pruebas del proyecto.");
        }
        if (u.getCorreo().equalsIgnoreCase(correoSesion)) {
            throw new ReglaNegocioException("No puedes eliminar la cuenta con la que estás trabajando.");
        }
        usuarioRepository.delete(u);
    }

    // ---- Métodos de apoyo ----

    public UsuarioResponse aRespuesta(Usuario u) {
        return UsuarioResponse.from(u, beneficioService.calcular(u));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }

    // Correo y RUN no se repiten (se ignora el mismo usuario al editar).
    private void validarUnicos(String correo, String run, Long idActual) {
        usuarioRepository.findByCorreoIgnoreCase(correo)
                .filter(otro -> !otro.getId().equals(idActual))
                .ifPresent(otro -> { throw new ConflictoException("Ya existe una cuenta con ese correo."); });

        if (run != null) {
            usuarioRepository.findByRun(run)
                    .filter(otro -> !otro.getId().equals(idActual))
                    .ifPresent(otro -> { throw new ConflictoException("Ya existe una cuenta con ese RUN."); });
        }
    }

    private void copiarDatos(UsuarioRequest req, Usuario u) {
        u.setRun(normalizarRun(req.run()));
        u.setNombre(req.nombre().trim());
        u.setApellidos(req.apellidos().trim());
        u.setCorreo(normalizarCorreo(req.correo()));
        u.setFechaNacimiento(req.fechaNacimiento());
        u.setTelefono(vacioANull(req.telefono()));
        u.setRegion(req.region());
        u.setComuna(req.comuna());
        u.setDireccion(vacioANull(req.direccion()));
        u.setCodigoPromocional(req.codigoPromocional() == null ? null : req.codigoPromocional().trim().toUpperCase());
        u.setRol(Rol.desdeTexto(req.rol()));
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private String normalizarRun(String run) {
        if (run == null || run.isBlank()) return null;
        return run.replace(".", "").replace(" ", "").toUpperCase();
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}