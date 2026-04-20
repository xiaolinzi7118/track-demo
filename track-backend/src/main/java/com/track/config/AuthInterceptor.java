package com.track.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        if (isWhitelisted(uri)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        String token = authHeader.substring(7);
        Long userId = tokenStore.getUserId(token);
        if (userId == null) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        request.setAttribute("currentUserId", userId);
        return true;
    }

    private boolean isWhitelisted(String uri) {
        return uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/track-data/report")
                || uri.startsWith("/api/track-data/batch-report")
                || uri.startsWith("/h2-console");
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
