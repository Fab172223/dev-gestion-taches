package fab.nkomoelate.taskmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Récupère le header Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Si pas de token → passe au filtre suivant
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrait le token (enlève "Bearer ")
        final String token = authHeader.substring(7);

        // 4. Extrait l'email depuis le token
        final String email = jwtService.extractEmail(token);

        // 5. Si email trouvé et user pas encore authentifié
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Charge le user depuis la BDD
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 7. Valide le token
            if (jwtService.isTokenValid(token, userDetails)) {

                // 8. Crée l'objet d'authentification
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Stocke dans le SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Passe au filtre suivant
        filterChain.doFilter(request, response);
    }
}