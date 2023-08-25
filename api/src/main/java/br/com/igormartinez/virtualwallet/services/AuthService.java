package br.com.igormartinez.virtualwallet.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import br.com.igormartinez.virtualwallet.data.dto.RegistrationDTO;
import br.com.igormartinez.virtualwallet.data.dto.UserDTO;
import br.com.igormartinez.virtualwallet.data.security.AccountCredentials;
import br.com.igormartinez.virtualwallet.data.security.Token;
import br.com.igormartinez.virtualwallet.exceptions.InvalidTokenException;
import br.com.igormartinez.virtualwallet.exceptions.InvalidUsernamePasswordException;
import br.com.igormartinez.virtualwallet.exceptions.RequestValidationException;
import br.com.igormartinez.virtualwallet.exceptions.ResourceAlreadyExistsException;
import br.com.igormartinez.virtualwallet.exceptions.ResourceNotFoundException;
import br.com.igormartinez.virtualwallet.exceptions.TokenCreationErrorException;
import br.com.igormartinez.virtualwallet.models.Role;
import br.com.igormartinez.virtualwallet.models.User;
import br.com.igormartinez.virtualwallet.repositories.RoleRepository;
import br.com.igormartinez.virtualwallet.repositories.UserRepository;
import br.com.igormartinez.virtualwallet.security.PasswordManager;
import br.com.igormartinez.virtualwallet.security.jwt.JwtTokenProvider;

@Service
public class AuthService {

    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordManager passwordManager;
    private final UserRepository repository;
    private final RoleRepository roleRepository;

    public AuthService(JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager,
            PasswordManager passwordManager, UserRepository repository, RoleRepository roleRepository) {
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.passwordManager = passwordManager;
        this.repository = repository;
        this.roleRepository = roleRepository;
    }

    public UserDTO signup(RegistrationDTO registration) {

        if (repository.findByEmailOrDocument(registration.email(), registration.document()).isPresent())
            throw new ResourceAlreadyExistsException("The email or document is already in use.");
        
        User user = new User();
        user.setName(registration.name());
        user.setDocument(registration.document());
        user.setEmail(registration.email());
        user.setPassword(passwordManager.encodePassword(registration.password()));
        user.setAccountNonExpired(Boolean.TRUE);
        user.setAccountNonLocked(Boolean.TRUE);
        user.setCredentialsNonExpired(Boolean.TRUE);
        user.setEnabled(Boolean.TRUE);

        Role role = roleRepository.findById(1L)
            .orElseThrow(() -> new ResourceNotFoundException("The role was not found with the given ID."));
        user.setRole(role);

        User createdUser = repository.save(user);

        return new UserDTO(
            createdUser.getId(), 
            createdUser.getName(), 
            createdUser.getDocument(), 
            createdUser.getEmail(), 
            createdUser.getRole().getDescription());
    }
    
    public Token signin(AccountCredentials accountCredentials) {
        String username = accountCredentials.username();
        String password = accountCredentials.password();
        
        User user = repository.findByEmail(username)
            .orElseThrow(() -> new InvalidUsernamePasswordException());
        
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            return tokenProvider.createAccessToken(username, user.getRole().getDescription());
        } catch (BadCredentialsException ex) {
            throw new InvalidUsernamePasswordException();
        } catch (JWTCreationException ex){
            throw new TokenCreationErrorException();
        }
    }

    public Token refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank())
            throw new RequestValidationException("The refresh token must be not blank.");
        
        try {
            return tokenProvider.refreshToken(refreshToken);
        } catch (JWTVerificationException ex){
            throw new InvalidTokenException("Invalid refresh token.");
        } catch (JWTCreationException ex){
            throw new TokenCreationErrorException();
        }
    }
}
