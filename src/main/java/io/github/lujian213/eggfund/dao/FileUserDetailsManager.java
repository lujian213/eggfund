package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.service.InvestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;


@Component
public class FileUserDetailsManager implements UserDetailsService {
    private InvestService investService;

    @Autowired
    public void setInvestService(InvestService investService) {
        this.investService = investService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return investService.getUser(username)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
    }

}



