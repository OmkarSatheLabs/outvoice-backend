package com.omkarsathe.outvoice.platform;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Validates platform JWTs and populates the SecurityContext for /api/platform/** routes. */
@RequiredArgsConstructor
@Slf4j
public class PlatformJwtAuthFilter extends OncePerRequestFilter {

    private final PlatformJwtService platformJwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (platformJwtService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                PlatformContext ctx = platformJwtService.extractContext(token);
                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_PLATFORM_" + ctx.role().name()));

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(ctx, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                log.warn("Failed to extract platform context from JWT: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
