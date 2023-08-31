package br.com.igormartinez.virtualwallet.exceptions.handlers;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomSpringSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    
    public record ErrorResponse(
        String type,
        String title,
        Integer status,
        String detail,
        String instance
    ) {
        public String toJsonString() {
            return "{\"type\":\""+type+"\",\"title\":\""+title+"\",\"status\":\""+status+"\",\"detail\":\""+detail+"\",\"instance\":\""+instance+"\"}";
        }
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/problem+json");
        ErrorResponse errorResponse = new ErrorResponse(
            "about:blank",
            "Forbidden", 
            HttpStatus.FORBIDDEN.value(), 
            "Authentication required.",
            request.getRequestURI());
        response.getWriter().write(errorResponse.toJsonString());
        response.setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exc) throws IOException {
        response.setContentType("application/problem+json");
        ErrorResponse errorResponse = new ErrorResponse(
            "about:blank",
            "Forbidden", 
            HttpStatus.FORBIDDEN.value(), 
            "Access denied.",
            request.getRequestURI());
        response.getWriter().write(errorResponse.toJsonString());
        response.setStatus(HttpStatus.FORBIDDEN.value());
    }

}
