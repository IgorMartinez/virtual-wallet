package br.com.igormartinez.virtualwallet.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.igormartinez.virtualwallet.data.dto.RegistrationDTO;
import br.com.igormartinez.virtualwallet.data.dto.UserDTO;
import br.com.igormartinez.virtualwallet.data.security.AccountCredentials;
import br.com.igormartinez.virtualwallet.data.security.Token;
import br.com.igormartinez.virtualwallet.services.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService service;

    @PostMapping("/signup")
    public UserDTO signup(@RequestBody @Valid RegistrationDTO registrationDTO) {
        return service.signup(registrationDTO);
    }

    @PostMapping("/signin")
    public Token signin(@RequestBody @Valid AccountCredentials accountCredentials) {
        return service.signin(accountCredentials);
    }
    
    @PutMapping("/refresh")
    public Token refresh(@RequestHeader("Authorization") String refreshToken) {
        return service.refresh(refreshToken);
    }
}
