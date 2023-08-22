package br.com.igormartinez.virtualwallet.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.igormartinez.virtualwallet.repositories.RoleRepository;
import br.com.igormartinez.virtualwallet.repositories.UserRepository;
import br.com.igormartinez.virtualwallet.security.PasswordManager;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;

    public UserService(UserRepository repository, PasswordManager passwordManager, RoleRepository roleRepository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    
}
