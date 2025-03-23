package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.Investor;
import io.github.lujian213.eggfund.service.InvestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FileUserDetailsManager implements UserDetailsService {
    private InvestService investService;

    @Autowired
    public void setInvestService(InvestService investService) {
        this.investService = investService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findInvestorByUsername(username)
                .map(this::convertToSpringUser)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
    }

    private Optional<Investor> findInvestorByUsername(String username) {
        String normalizedUsername = username.toLowerCase(Locale.ROOT);
        return investService.getAllInvestors().stream()
                .filter(investor -> normalizedUsername.equals(investor.getId().toLowerCase(Locale.ROOT)))
                .findFirst();
    }

    private UserDetails convertToSpringUser(Investor investor) {
        return new User(
                investor.getId(),
                investor.getPassword(),
                mapRolesToAuthorities(investor.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();
    }
}



