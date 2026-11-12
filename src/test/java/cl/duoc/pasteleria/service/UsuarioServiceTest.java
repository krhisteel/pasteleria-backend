package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.UsuarioRequest;
import cl.duoc.pasteleria.dto.UsuarioResponse;
import cl.duoc.pasteleria.entity.Rol;
import cl.duoc.pasteleria.entity.Usuario;
import cl.duoc.pasteleria.exception.ConflictoException;
import cl.duoc.pasteleria.exception.ReglaNegocioException;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Spy BeneficioService beneficioService = new BeneficioService();
    @InjectMocks UsuarioService usuarioService;

    private UsuarioRequest nuevaCliente() {
        return new UsuarioRequest(null, "Ana", "Pérez", "ana@duoc.cl", "clave1",
                null, null, null, null, null, null, "cliente");
    }

    @Test
    void creaUsuarioConContrasenaEncriptada() {
        when(usuarioRepository.findByCorreoIgnoreCase(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave1")).thenReturn("HASH");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse r = usuarioService.crear(nuevaCliente());

        verify(usuarioRepository).save(argThat(u -> u.getPassword().equals("HASH")));
        assertEquals("cliente", r.rol());
        assertTrue(r.tortaGratisDuoc()); // correo @duoc.cl
    }

    @Test
    void noPermiteCorreoRepetido() {
        when(usuarioRepository.findByCorreoIgnoreCase("ana@duoc.cl"))
                .thenReturn(Optional.of(Usuario.builder().id(9L).correo("ana@duoc.cl").build()));

        assertThrows(ConflictoException.class, () -> usuarioService.crear(nuevaCliente()));
    }

    @Test
    void noPermiteEliminarCuentaDemo() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(Usuario.builder().id(1L).correo("admin@duoc.cl").rol(Rol.ADMIN).build()));

        assertThrows(ReglaNegocioException.class, () -> usuarioService.eliminar(1L, "otro@duoc.cl"));
    }
}