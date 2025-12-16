package com.hyuns.cafit.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyuns.cafit.dto.ErrorResponse;
import com.hyuns.cafit.errors.ErrorMessage;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class LoginFilter implements Filter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> EXACT_WHITELIST = Arrays.asList(
            "/", "/api/auth/login", "/api/auth/signup"
    );

    private static final List<String> PREFIX_WHITELIST = List.of(
            "/css/", "/js/", "/images/"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String requestURI = httpRequest.getRequestURI();
        HttpSession session = httpRequest.getSession(false);

        log.info("Request URI: {}", requestURI);

        if (isExcludedUrl(requestURI)) {
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        if (session == null || session.getAttribute("userId") == null) {
            sendErrorResponse(httpResponse, ErrorMessage.UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(httpRequest, httpResponse);
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorMessage errorMessage) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.of(
                errorMessage.getCode(),
                errorMessage.getMessage()
        );

        response.setStatus(errorMessage.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private boolean isExcludedUrl(String requestURI) {
        if (EXACT_WHITELIST.contains(requestURI)) {
            return true;
        }
        return PREFIX_WHITELIST.stream()
                .anyMatch(requestURI::startsWith);
    }
}
