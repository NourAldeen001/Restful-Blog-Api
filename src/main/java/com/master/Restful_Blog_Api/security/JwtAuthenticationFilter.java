package com.master.Restful_Blog_Api.security;

import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Order(2)
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // In Header : "Authorization"
            String token = getJwtFromRequestHeader(request);

            if(StringUtils.hasText(token)) {
                log.debug("JWT token found in request: path={}", request.getRequestURI());
                // Extract email (Signature is validated secretly)
                String email = jwtTokenProvider.getEmailFromToken(token);
                // check expire & email match
                if(jwtTokenProvider.validateToken(token, email)) {
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user == null) {
                        log.warn("Valid JWT but user not found in DB: email={}", email);
                    } else {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null, // Credentials (not needed after authentication)
                                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                                );

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("Authentication set: email={}, role={}, path={}",
                                email, user.getRole(), request.getRequestURI());
                    }
                }
            }
        }
        catch(Exception ex) {
            // Could not set user authentication in security context
            log.warn("Failed to set authentication for request {}: {}",
                    request.getRequestURI(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // Extract JWT token from Authorization header
    // Header format: "Authorization: Bearer <token>"

    private String getJwtFromRequestHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
