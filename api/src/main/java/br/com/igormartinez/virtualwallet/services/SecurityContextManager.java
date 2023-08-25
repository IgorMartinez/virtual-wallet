package br.com.igormartinez.virtualwallet.services;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.com.igormartinez.virtualwallet.models.User;

@Service
public class SecurityContextManager {
    
    private boolean verifyIdUserAuthenticated(long id) {
        User userAuthenticated = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return id == userAuthenticated.getId();
    }

    /**
     * Verify if the id of user of authorization token is the same of the param
     * @param id
     * @return boolean - if the id param is the same of token 
     */
    public boolean checkSameUser(long id) {
        return verifyIdUserAuthenticated(id);
    }

}
