package cl.duoc.pasteleria.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Corre en cada petición: lee "Authorization: Bearer <token>",
// lo valida y deja al usuario autenticado para esa petición.
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Sin token: sigue sin autenticar (las rutas protegidas responderán 401).
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (jwtService.esAccessTokenValido(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String correo = jwtService.leerClaims(token).getSubject();
                UserDetails usuario = usuarioDetailsService.loadUserByUsername(correo);

                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (UsernameNotFoundException e) {
                // El usuario fue eliminado: sigue sin autenticar (responderá 401).
            }
        }

        chain.doFilter(request, response);
    }
}