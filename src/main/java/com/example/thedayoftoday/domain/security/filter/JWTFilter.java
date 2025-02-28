package com.example.thedayoftoday.domain.security.filter;

import com.example.thedayoftoday.domain.security.CustomUserDetails;
import com.example.thedayoftoday.domain.security.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (requestURI.equals("/") || requestURI.equals("/login") || requestURI.equals("/reissue")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = request.getHeader("access");

        System.out.println("Received access token: " + accessToken);

        // 토큰이 없거나 빈 값이면 필터 진행
        if (accessToken == null || accessToken.trim().isEmpty()) {
            System.out.println("No access token provided, proceeding without authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        // 공백이 포함된 경우 제거
        accessToken = accessToken.trim();

        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("access token expired");
            return;
        }

        String category = jwtUtil.getCategory(accessToken);
        if (!"access".equals(category)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("invalid access token");
            return;
        }

        String email = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        CustomUserDetails customUserDetails = new CustomUserDetails(email, role);

        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}