package br.com.igormartinez.virtualwallet.unittests.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.igormartinez.virtualwallet.data.dto.UserDTO;
import br.com.igormartinez.virtualwallet.exceptions.RequestValidationException;
import br.com.igormartinez.virtualwallet.exceptions.ResourceNotFoundException;
import br.com.igormartinez.virtualwallet.exceptions.UserUnauthorizedException;
import br.com.igormartinez.virtualwallet.mocks.UserMock;
import br.com.igormartinez.virtualwallet.models.User;
import br.com.igormartinez.virtualwallet.repositories.UserRepository;
import br.com.igormartinez.virtualwallet.services.SecurityContextManager;
import br.com.igormartinez.virtualwallet.services.UserService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    private UserService service;
    private UserMock userMock;

    @Mock
    private UserRepository repository;

    @Mock
    private SecurityContextManager securityContextManager;

    @BeforeEach
    void setUp() {
        userMock = new UserMock();
        service = new UserService(repository, securityContextManager);
    }

    @Test
    public void testLoadUserByUsernameWithNullParam() {
        when(repository.findByEmail(null)).thenReturn(Optional.ofNullable(null));

        Exception output = assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(null);
        });
        String expectedMessage = "User not found.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    public void testLoadUserByUsernameWithFindUser(){
        User mockedUser = userMock.mockEntity(1);
        
        when(repository.findByEmail(mockedUser.getEmail())).thenReturn(Optional.of(mockedUser));

        UserDetails output = service.loadUserByUsername(mockedUser.getEmail());
        assertEquals("user_mail1@test.com", output.getUsername());
        assertEquals("password1", output.getPassword());
        assertEquals(1, output.getAuthorities().size());
        assertTrue(output.getAuthorities()
            .stream().anyMatch(ga -> ga.getAuthority().equals("Role description 1"))
        );
    }

    @Test
    public void testLoadUserByUsernameWithNotFindUser() {
        when(repository.findByEmail("notfinduser@byemail")).thenReturn(Optional.ofNullable(null));

        Exception output = assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername("notfinduser@byemail");
        });
        String expectedMessage = "User not found";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    public void testFindByIdWithParamNull() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.findById(null);
        });
        String expectedMessage = "The user-id must be a positive integer value.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    public void testFindByIdWithParamZero() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.findById(0L);
        });
        String expectedMessage = "The user-id must be a positive integer value.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    public void testFindByIdWithParamNegative() {
        Exception output = assertThrows(RequestValidationException.class, () -> {
            service.findById(-1231L);
        });
        String expectedMessage = "The user-id must be a positive integer value.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    public void testFindByIdWithPermission() {
        User mockedUser = userMock.mockEntity(1);

        when(securityContextManager.checkSameUser(1)).thenReturn(Boolean.TRUE);
        when(repository.findById(1L)).thenReturn(Optional.of(mockedUser));

        UserDTO output = service.findById(1L);
        assertEquals(Long.valueOf(1L), output.id());
        assertEquals("User name 1", output.name());
        assertEquals("000.000.000-01", output.document());
        assertEquals("user_mail1@test.com", output.email());
        assertEquals(new BigDecimal("1.99"), output.accountBalance());
        assertEquals("Role description 1", output.role());
    }

    @Test
    public void testFindByIdWithoutPermission() {
        when(securityContextManager.checkSameUser(1L)).thenReturn(Boolean.FALSE);
        
        Exception output = assertThrows(UserUnauthorizedException.class, () -> {
            service.findById(1L);
        });
        String expectedMessage = "The user is not authorized to access this resource.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }

    @Test
    public void testFindByIdWithNotFoundUser() {
        when(securityContextManager.checkSameUser(1L)).thenReturn(Boolean.TRUE);
        when(repository.findById(1L)).thenReturn(Optional.ofNullable(null));

        Exception output = assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(1L);
        });
        String expectedMessage = "The user was not found with the given ID.";
        assertTrue(output.getMessage().contains(expectedMessage));
    }
}
