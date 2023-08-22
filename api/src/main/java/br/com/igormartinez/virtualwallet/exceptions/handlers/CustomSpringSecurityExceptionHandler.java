package br.com.igormartinez.virtualwallet.exceptions.handlers;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import br.com.igormartinez.virtualwallet.exceptions.ExceptionResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomSpringSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        ExceptionResponse exceptionResponse = 
            new ExceptionResponse(
                "about:blank",
                "Forbidden", 
                HttpStatus.FORBIDDEN.value(), 
                authException.getMessage(), // Authentication required
                request.getRequestURI());
        response.getWriter().write(exceptionResponse.toJsonString());
        response.setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exc) throws IOException {
        response.setContentType("application/json");
        ExceptionResponse exceptionResponse = 
            new ExceptionResponse(
                "about:blank",
                "Forbidden", 
                HttpStatus.FORBIDDEN.value(), 
                exc.getMessage(), // Access denied
                request.getRequestURI());
        response.getWriter().write(exceptionResponse.toJsonString());
        response.setStatus(HttpStatus.FORBIDDEN.value());
    }

}