package cl.duoc.pasteleria.security;

import cl.duoc.pasteleria.entity.Usuario;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Le dice a Spring Security cómo buscar un usuario por correo y cuál es su rol.
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // roles("ADMIN") se transforma en la autoridad "ROLE_ADMIN".
        return User.withUsername(u.getCorreo())
                .password(u.getPassword())
                .roles(u.getRol().name())
                .build();
    }
}