package br.com.igormartinez.virtualwallet.exceptions.handlers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.igormartinez.virtualwallet.exceptions.InvalidTokenException;
import br.com.igormartinez.virtualwallet.exceptions.RequestValidationException;
import br.com.igormartinez.virtualwallet.exceptions.ResourceAlreadyExistsException;
import br.com.igormartinez.virtualwallet.exceptions.TokenCreationErrorException;
import br.com.igormartinez.virtualwallet.exceptions.InvalidUsernamePasswordException;

@ControllerAdvice
@RestController
public class CustomizedExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, 
        HttpStatusCode status, WebRequest request) {

        ProblemDetail problemDetail = ex.getBody();

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.put(error.getObjectName(), error.getDefaultMessage());
        }
        problemDetail.setProperty("errors", errors);

        return super.createResponseEntity(problemDetail, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({TokenCreationErrorException.class, Exception.class})
    public final ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
        ProblemDetail problemDetail = super.createProblemDetail(
                ex, 
                HttpStatus.INTERNAL_SERVER_ERROR, 
                ex.getMessage(), 
                null, 
                null, 
                request);

        HttpHeaders headers = new HttpHeaders();
        return super.createResponseEntity(problemDetail, headers, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler({
        RequestValidationException.class,
        BadCredentialsException.class, 
        UsernameNotFoundException.class})
    public final ResponseEntity<?> handleBadRequestExceptions(Exception ex, WebRequest request) {
        ProblemDetail problemDetail = super.createProblemDetail(
                ex, 
                HttpStatus.BAD_REQUEST, 
                ex.getMessage(), 
                null, 
                null, 
                request);

        HttpHeaders headers = new HttpHeaders();
        return super.createResponseEntity(problemDetail, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({
        InvalidUsernamePasswordException.class,
        InvalidTokenException.class})
    public final ResponseEntity<?> handleUnauthorizedExceptions(Exception ex, WebRequest request) {
        ProblemDetail problemDetail = super.createProblemDetail(
                ex, 
                HttpStatus.UNAUTHORIZED, 
                ex.getMessage(), 
                null, 
                null, 
                request);
        
        HttpHeaders headers = new HttpHeaders();
        return super.createResponseEntity(problemDetail, headers, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public final ResponseEntity<?> handleConflictExceptions(Exception ex, WebRequest request) {
        ProblemDetail problemDetail = super.createProblemDetail(
                ex, 
                HttpStatus.CONFLICT, 
                ex.getMessage(), 
                null, 
                null, 
                request);
        
        HttpHeaders headers = new HttpHeaders();
        return super.createResponseEntity(problemDetail, headers, HttpStatus.CONFLICT, request);
    }
}
