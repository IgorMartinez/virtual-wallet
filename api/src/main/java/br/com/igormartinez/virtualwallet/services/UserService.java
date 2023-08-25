package br.com.igormartinez.virtualwallet.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.igormartinez.virtualwallet.data.dto.UserDTO;
import br.com.igormartinez.virtualwallet.exceptions.RequestValidationException;
import br.com.igormartinez.virtualwallet.exceptions.ResourceNotFoundException;
import br.com.igormartinez.virtualwallet.exceptions.UserUnauthorizedException;
import br.com.igormartinez.virtualwallet.models.User;
import br.com.igormartinez.virtualwallet.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final SecurityContextManager securityContextManager;

    public UserService(UserRepository repository, SecurityContextManager securityContextManager) {
        this.repository = repository;
        this.securityContextManager = securityContextManager;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // TODO: User not found in authentication throw status 500
        return repository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    public UserDTO findById(Long id) {
        if (id == null || id <= 0)
            throw new RequestValidationException("The user-id must be a positive integer value.");

        if (!securityContextManager.checkSameUser(id))
            throw new UserUnauthorizedException();
        
        User user = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("The user was not found with the given ID."));

        return new UserDTO(
            user.getId(), 
            user.getName(), 
            user.getDocument(), 
            user.getEmail(), 
            user.getAccountBalance(),
            user.getRole().getDescription());
    }
    
}
