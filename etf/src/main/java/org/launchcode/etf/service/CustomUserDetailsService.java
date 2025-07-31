package org.launchcode.etf.service;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.launchcode.etf.dao.UserDao;
import org.launchcode.etf.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    private final UserDao userDao;

    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // hasRole('ADMIN') checks for authorities "ROLE_ADMIN".
        // So, we need to add "ROLE_" prefix to the role. You must write "ADMIN" in the database. "admin" won't work.
        // In the given example of CustomUserDetailsService in Launchcode final assignment,hasRole('ADMIN') checks for authorities "ROLE_ADMIN".
        // So, we need to add "ROLE_ADMIN" in mysql database.
        // If you want to use hasRole('USER'), you need to add "ROLE_USER" in mysql database. 
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        // Create JwtUser object with necessary fields
        // JwtUser is a class that extends UserDetails
        JwtUser jwtUser = new JwtUser();
        jwtUser.setUsername(user.getUsername());
        jwtUser.setPassword(user.getPassword());
        jwtUser.setAuthorities(authorities);
        jwtUser.setAccountNonExpired(true);
        jwtUser.setAccountNonLocked(true);
        jwtUser.setApiAccessAllowed(true);
        jwtUser.setCredentialsNonExpired(true);
        jwtUser.setEnabled(true);
        
        return jwtUser;
    }
}