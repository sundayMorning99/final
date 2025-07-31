package org.launchcode.etf.controller;

import org.launchcode.etf.dao.UserDao;
import org.launchcode.etf.dto.RegisterRequest;
import org.launchcode.etf.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class RegistrationController {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody RegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        
        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        
        String trimmedUsername = registerRequest.getUsername().trim();
        
        if (userDao.getUserByUsername(trimmedUsername) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        try {
            User user = new User();
            user.setUsername(trimmedUsername);
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole("USER");

            userDao.save(user);
            return "User registered successfully";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user");
        }
    }
}