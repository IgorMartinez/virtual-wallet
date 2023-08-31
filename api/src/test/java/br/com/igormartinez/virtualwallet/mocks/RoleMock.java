package br.com.igormartinez.virtualwallet.mocks;

import br.com.igormartinez.virtualwallet.models.Role;

public class RoleMock {
    public Role mockEntity(int number) {
        Role role = new Role();
        role.setId(Long.valueOf(number));
        role.setDescription("Description " + number);
        return role;
    }

    public Role mockEntity(int number, String description) {
        Role role = new Role();
        role.setId(Long.valueOf(number));
        role.setDescription(description);
        return role;
    }
}
