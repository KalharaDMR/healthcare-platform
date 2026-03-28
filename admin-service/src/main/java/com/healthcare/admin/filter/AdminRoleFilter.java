package com.healthcare.admin.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AdminRoleFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        // Only protect /api/admin/specializations POST (or all admin endpoints if needed)
        if (path.startsWith("/api/admin/specializations") && "POST".equalsIgnoreCase(request.getMethod())) {
            String role = request.getHeader("X-User-Role");
            if (role == null || !role.contains("ADMIN")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Only admin can add specializations\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}