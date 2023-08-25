package br.com.igormartinez.virtualwallet.unittests.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

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
import br.com.igormartinez.virtualwallet.mocks.RoleMock;
import br.com.igormartinez.virtualwallet.mocks.TokenMock;
import br.com.igormartinez.virtualwallet.mocks.UserMock;
import br.com.igormartinez.virtualwallet.models.Role;
import br.com.igormartinez.virtualwallet.models.User;
import br.com.igormartinez.virtualwallet.repositories.RoleRepository;
import br.com.igormartinez.virtualwallet.repositories.UserRepository;
import br.com.igormartinez.virtualwallet.security.PasswordManager;
import br.com.igormartinez.virtualwallet.security.jwt.JwtTokenProvider;
import br.com.igormartinez.virtualwallet.services.AuthService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    
    private AuthService service;
    private UserMock userMock;
    private RoleMock roleMock;
    private TokenMock tokenMock;
    
    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private PasswordManager passwordManager;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        userMock = new UserMock();
        roleMock = new RoleMock();
        tokenMock = new TokenMock();

        service = new AuthService(
            tokenProvider,
            authenticationManager,
            passwordManager,
            userRepository, 
            roleRepository);
    }

    @Test
    void testSignupWithExistingUser() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "Name 1", "000.000.000-00", "email@email.com", "1234");

        when(userRepository.findByEmailOrDocument(registrationDTO.email(), registrationDTO.document()))
            .thenReturn(Optional.of(userMock.mockEntity(1)));

        Exception output = assertThrows(ResourceAlreadyExistsException.class, () -> {
            service.signup(registrationDTO);
        });
        String expectedMessage = "The email or document is already in use.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testSignUpWithNotExistingUser(){
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "Name 1", "000.000.000-00", "email@email.com", "1234");
        Role mockedRole = roleMock.mockEntity(1);
        User mockedUser = userMock.mockEntity(1, registrationDTO, mockedRole);

        when(userRepository.findByEmailOrDocument(registrationDTO.email(), registrationDTO.document()))
            .thenReturn(Optional.ofNullable(null));
        when(passwordManager.encodePassword(registrationDTO.password()))
            .thenReturn("encodedPassword");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(mockedRole));
        when(userRepository.save(any(User.class))).thenReturn(mockedUser);

        UserDTO output = service.signup(registrationDTO);
        assertEquals(1L, output.id());
        assertEquals("Name 1", output.name());
        assertEquals("000.000.000-00", output.document());
        assertEquals("email@email.com", output.email());
        assertEquals(new BigDecimal("1.99"), output.accountBalance());
        assertEquals("Description 1", output.role());

        // Verify the argument just before send to save in database
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(argumentCaptor.capture());
        User capturedObject = argumentCaptor.getValue();assertNotNull(capturedObject);
        assertNull(capturedObject.getId());
        assertEquals("Name 1", capturedObject.getName());
        assertEquals("000.000.000-00", capturedObject.getDocument());
        assertEquals("email@email.com", capturedObject.getEmail());
        assertEquals("encodedPassword", capturedObject.getPassword());
        assertEquals(new BigDecimal("0.00"), capturedObject.getAccountBalance());
        assertTrue(capturedObject.getAccountNonExpired());
        assertTrue(capturedObject.getAccountNonLocked());
        assertTrue(capturedObject.getCredentialsNonExpired());
        assertTrue(capturedObject.getEnabled());
        assertEquals(1L, capturedObject.getRole().getId());
        assertEquals("Description 1", capturedObject.getRole().getDescription());
    }

    @Test
    void testSignupWithRoleNotFound() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "Name 1", "000.000.000-00", "email@email.com", "1234");

        when(userRepository.findByEmailOrDocument(registrationDTO.email(), registrationDTO.document()))
            .thenReturn(Optional.ofNullable(null));
        when(passwordManager.encodePassword(registrationDTO.password()))
            .thenReturn("encodedPassword");
        when(roleRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        Exception output = assertThrows(ResourceNotFoundException.class, () -> {
            service.signup(registrationDTO);
        });
        String expectedMessage = "The role was not found with the given ID.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testSigninWithUserNotFound() {
        AccountCredentials accountCredentials = new AccountCredentials("test", "test");

        when(userRepository.findByEmail("test")).thenReturn(Optional.ofNullable(null));

        Exception output = assertThrows(InvalidUsernamePasswordException.class, () -> {
            service.signin(accountCredentials);
        });
        String expectedMessage = "Invalid email or password.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testSigninWithWrongPassword() {
        AccountCredentials accountCredentials = new AccountCredentials("test", "test");
        User user = userMock.mockEntity(1);

        when(userRepository.findByEmail("test")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        Exception output = assertThrows(InvalidUsernamePasswordException.class, () -> {
            service.signin(accountCredentials);
        });
        String expectedMessage = "Invalid email or password.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testSigninWithTokenCreationError() {
        AccountCredentials accountCredentials = new AccountCredentials("test", "test");
        User user = userMock.mockEntity(1);

        when(userRepository.findByEmail("test")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(tokenProvider.createAccessToken("test", user.getRole().getDescription()))
            .thenThrow(new JWTCreationException(null, null));

        Exception output = assertThrows(TokenCreationErrorException.class, () -> {
            service.signin(accountCredentials);
        });
        String expectedMessage = "There was an error while creating the JWT token";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testSigninWithTokenCreationSuccess() {
        AccountCredentials accountCredentials = new AccountCredentials("test", "test");
        User user = userMock.mockEntity(1);
        Token token = tokenMock.mockToken(accountCredentials.username());

        ZonedDateTime expectedCreatedTime = ZonedDateTime.of(
                2023, 
                06, 
                13, 
                13, 
                27, 
                0, 
                0, 
                ZoneId.systemDefault());

        ZonedDateTime expectedExpirationTime = ZonedDateTime.of(
                2023, 
                06, 
                13, 
                14, 
                27, 
                0, 
                0, 
                ZoneId.systemDefault());

        when(userRepository.findByEmail("test")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(tokenProvider.createAccessToken("test", user.getRole().getDescription()))
            .thenReturn(token);

        Token output = service.signin(accountCredentials);
        assertNotNull(output);
        assertEquals("test", output.getUsername());
        assertEquals(Boolean.TRUE, output.getAuthenticated());
        assertTrue(expectedCreatedTime.isEqual(output.getCreated()));
        assertTrue(expectedExpirationTime.isEqual(output.getExpiration()));
        assertEquals("mockedAccessToken", output.getAccessToken());
        assertEquals("mockedRefreshToken", output.getRefreshToken());
    }

    @Test
    void testRefreshWithParamNull() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.refresh(null);
        });
        String expectedMessage = "The refresh token must be not blank.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testRefreshWithParamBlank() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.refresh("");
        });
        String expectedMessage = "The refresh token must be not blank.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testRefreshWithInvalidToken() {
        when(tokenProvider.refreshToken("mockedRefreshToken"))
            .thenThrow(new JWTVerificationException(null));

        Exception output = assertThrows(InvalidTokenException.class, () -> {
            service.refresh("mockedRefreshToken");
        });
        String expectedMessage = "Invalid refresh token";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testRefreshWithTokenCreationError() {
        when(tokenProvider.refreshToken("mockedRefreshToken"))
            .thenThrow(new JWTCreationException(null, null));

        Exception output = assertThrows(TokenCreationErrorException.class, () -> {
            service.refresh("mockedRefreshToken");
        });
        String expectedMessage = "There was an error while creating the JWT token";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    void testRefreshWithTokenCreationSuccess() {
        Token token = tokenMock.mockToken("test");

        ZonedDateTime expectedCreatedTime = ZonedDateTime.of(
                2023, 
                06, 
                13, 
                13, 
                27, 
                0, 
                0, 
                ZoneId.systemDefault());

        ZonedDateTime expectedExpirationTime = ZonedDateTime.of(
                2023, 
                06, 
                13, 
                14, 
                27, 
                0, 
                0, 
                ZoneId.systemDefault());
        
        when(tokenProvider.refreshToken("mockedRefreshToken")).thenReturn(token);

        Token output = service.refresh("mockedRefreshToken");
        assertNotNull(output);
        assertEquals("test", output.getUsername());
        assertEquals(Boolean.TRUE, output.getAuthenticated());
        assertTrue(expectedCreatedTime.isEqual(output.getCreated()));
        assertTrue(expectedExpirationTime.isEqual(output.getExpiration()));
        assertEquals("mockedAccessToken", output.getAccessToken());
        assertEquals("mockedRefreshToken", output.getRefreshToken());
    }
}
