package org.launchcode.etf.controller;

import org.launchcode.etf.dao.UserDao;
import org.launchcode.etf.dto.ChangePasswordRequest;
import org.launchcode.etf.dto.CreateUserRequest;
import org.launchcode.etf.dto.UpdateUserRequest;
import org.launchcode.etf.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public User getCurrentUser(Principal principal) {
        User user = userDao.getUserByUsername(principal.getName());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.setPassword(null);
        return user;
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public void changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        
        if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is required");
        }
        
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
        }
        
        User user = userDao.getUserByUsername(principal.getName());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        userDao.updatePassword(user.getId(), newPasswordHash);
    }

    //only admin can manage users
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers(@RequestParam(required = false) String search) {
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userDao.search(search);
        } else {
            users = userDao.findAll();
        }

        users.forEach(user -> user.setPassword(null));
        return users;
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public User getUserById(@PathVariable Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.setPassword(null);
        return user;
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(@RequestBody CreateUserRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        
        String trimmedUsername = request.getUsername().trim();
        
        if (userDao.getUserByUsername(trimmedUsername) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        User user = new User();
        user.setUsername(trimmedUsername);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().trim());

        User savedUser = userDao.save(user);
        savedUser.setPassword(null);
        return savedUser;
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        
        User existingUser = userDao.findById(id);
        if (existingUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        String trimmedUsername = request.getUsername().trim();
        
        if (userDao.usernameExists(trimmedUsername, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        existingUser.setUsername(trimmedUsername);
        existingUser.setRole(request.getRole().trim());
        
        User updatedUser = userDao.update(existingUser);

        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
            userDao.updatePassword(id, newPasswordHash);
        }

        updatedUser.setPassword(null);
        return updatedUser;
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id, Principal principal) {
        User userToDelete = userDao.findById(id);
        if (userToDelete == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User currentUser = userDao.getUserByUsername(principal.getName());
        if (currentUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete your own account");
        }

        userDao.deleteById(id);
    }
}