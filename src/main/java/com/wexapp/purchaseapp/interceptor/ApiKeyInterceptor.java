package com.wexapp.purchaseapp.interceptor;

import com.wexapp.purchaseapp.service.ApiKeyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Autowired
    private ApiKeyService apiKeyService;

    @Value("${default.api.key:}")
    private String defaultApiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow OPTIONS requests for CORS
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // Get API key from header or query parameter
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = request.getParameter("apiKey");
        }

        // If no API key provided, try using default from environment variable
        if ((apiKey == null || apiKey.isEmpty()) && defaultApiKey != null && !defaultApiKey.isEmpty()) {
            apiKey = defaultApiKey;
        }

        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"API key is required. Please provide X-API-Key header or apiKey query parameter.\"}");
            return false;
        }

        if (!apiKeyService.isValidApiKey(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or expired API key.\"}");
            return false;
        }

        return true;
    }
}

