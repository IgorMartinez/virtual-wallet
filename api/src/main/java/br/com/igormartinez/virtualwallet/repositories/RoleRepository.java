package br.com.igormartinez.virtualwallet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.igormartinez.virtualwallet.models.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    
}
