package org.launchcode.etf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {
    
    // If you go to 'bcrypt-generator.com', the default cost factor is 12. BCryptPasswordEncoder uses a cost factor of 10 by default.
    // When you add user accounts in SQL in MySQL Workbench, make sure to use the same cost factor.
    // If you use a different cost factor, the password won't match.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}