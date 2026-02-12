package github.maxsuel.agregadordeinvestimentos.config;

import java.io.IOException;
import java.util.List;

import github.maxsuel.agregadordeinvestimentos.service.BlacklistService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import github.maxsuel.agregadordeinvestimentos.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final BlacklistService blacklistService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);

        if (token != null) {
            if (blacklistService.getBlacklistedToken(token) != null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been invalidated (Logged out).");

                return;
            }

            var login = tokenService.validateToken(token);

            if (!login.isEmpty()) {
                var userOptional = userRepository.findByUsername(login);

                if (userOptional.isPresent()) {
                    var user = userOptional.get();
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

                    var authentication = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(authority)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private @Nullable String recoverToken(@NonNull HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            return null;
        }

        return authHeader.replace("Bearer ", "");
    }

}
