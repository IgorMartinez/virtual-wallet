package br.com.igormartinez.virtualwallet.mocks;

import br.com.igormartinez.virtualwallet.data.dto.RegistrationDTO;
import br.com.igormartinez.virtualwallet.models.Role;
import br.com.igormartinez.virtualwallet.models.User;

public class UserMock {

    public User mockEntity(int number) {
        User user = new User();
        user.setId(Long.valueOf(number));
        user.setName("User name " + number);
        user.setDocument("000.000.000-"+String.format("%02d", number%100));
        user.setEmail("user_mail" + number + "@test.com");
        user.setPassword("password" + number);
        user.setAccountNonExpired((number%2==0) ? Boolean.FALSE : Boolean.TRUE);
        user.setAccountNonLocked((number%2==0) ? Boolean.FALSE : Boolean.TRUE);
        user.setCredentialsNonExpired((number%2==0) ? Boolean.FALSE : Boolean.TRUE);
        user.setEnabled((number%2==0) ? Boolean.FALSE : Boolean.TRUE);

        Role role = new Role();
        role.setId(Long.valueOf(number));
        role.setDescription("Role description " + number);
        user.setRole(role);

        return user;
    }

    public User mockEntity(int number, RegistrationDTO registrationDTO, Role role) {
        User user = new User();
        user.setId(Long.valueOf(number));
        user.setName(registrationDTO.name());
        user.setDocument(registrationDTO.document());
        user.setEmail(registrationDTO.email());
        user.setPassword(registrationDTO.password());
        user.setAccountNonExpired((number%2==0) ? Boolean.FALSE : Boolean.TRUE);
        user.setAccountNonLocked((number%2==0) ? Boolean.FALSE : Boolean.TRUE);
        user.setCredentialsNonExpired((number%2==0) ? Boolean.FALSE : Boolean.TRUE);
        user.setEnabled((number%2==0) ? Boolean.FALSE : Boolean.TRUE);
        user.setRole(role);

        return user;
    }
}
