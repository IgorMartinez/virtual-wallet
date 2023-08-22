package br.com.igormartinez.virtualwallet.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.igormartinez.virtualwallet.exceptions.ExceptionResponse;
import br.com.igormartinez.virtualwallet.exceptions.InvalidTokenException;
import br.com.igormartinez.virtualwallet.exceptions.RequestValidationException;
import br.com.igormartinez.virtualwallet.exceptions.TokenCreationErrorException;
import br.com.igormartinez.virtualwallet.exceptions.InvalidUsernamePasswordException;

@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHander extends ResponseEntityExceptionHandler {

    // Example:
    // request.getDescription(false) = uri=/auth/signin
    // request.getDescription(false).substring(SUBSTRING_URI) = /auth/signin
    private final int SUBSTRING_URI = 4;

    @ExceptionHandler({TokenCreationErrorException.class, Exception.class})
    public final ResponseEntity<ExceptionResponse> handleAllExceptions(Exception ex, WebRequest request) {
        ExceptionResponse exceptionResponse = 
            new ExceptionResponse(
                "about:blank",
                "Internal Server Error", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                ex.getMessage(),
                request.getDescription(false).substring(SUBSTRING_URI));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({
        RequestValidationException.class,
        BadCredentialsException.class, 
        UsernameNotFoundException.class})
    public final ResponseEntity<ExceptionResponse> handleBadRequestExceptions(Exception ex, WebRequest request) {
        ExceptionResponse exceptionResponse = 
            new ExceptionResponse(
                "about:blank",
                "Bad Request", 
                HttpStatus.BAD_REQUEST.value(), 
                ex.getMessage(), 
                request.getDescription(false).substring(SUBSTRING_URI));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
        InvalidUsernamePasswordException.class,
        InvalidTokenException.class})
    public final ResponseEntity<ExceptionResponse> handleUnauthorizedExceptions(Exception ex, WebRequest request) {
        ExceptionResponse exceptionResponse = 
            new ExceptionResponse(
                "about:blank",
                "Unauthorized", 
                HttpStatus.UNAUTHORIZED.value(), 
                ex.getMessage(), 
                request.getDescription(false).substring(SUBSTRING_URI));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.UNAUTHORIZED);
    }
}
