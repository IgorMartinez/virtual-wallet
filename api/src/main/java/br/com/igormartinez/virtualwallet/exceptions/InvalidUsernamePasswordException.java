package br.com.igormartinez.virtualwallet.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidUsernamePasswordException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidUsernamePasswordException(String ex) {
        super(ex);
    }

    public InvalidUsernamePasswordException() {
        super("Invalid email or password.");
    }
}
