package br.com.igormartinez.virtualwallet.unittests.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.igormartinez.virtualwallet.mocks.UserMock;
import br.com.igormartinez.virtualwallet.models.User;
import br.com.igormartinez.virtualwallet.repositories.UserRepository;
import br.com.igormartinez.virtualwallet.services.UserService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    private UserService service;
    private UserMock userMock;

    @Mock
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        userMock = new UserMock();
        service = new UserService(repository);
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
}
