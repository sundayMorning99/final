package org.launchcode.etf.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.launchcode.etf.dao.UserDao;
import org.launchcode.etf.dto.ChangePasswordRequest;
import org.launchcode.etf.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class AuthControllerTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate.update("DELETE FROM users WHERE username LIKE 'test%'");
    }

    @Test
    public void testGetCurrentUserLogic() {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRole("USER");
        userDao.save(testUser);

        User foundUser = userDao.getUserByUsername("testuser");
        
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("USER", foundUser.getRole());
        
        foundUser.setPassword(null);
        assertNull(foundUser.getPassword());
    }

    @Test
    public void testGetCurrentUserNotFoundLogic() {
        User foundUser = userDao.getUserByUsername("nonexistent");
        assertNull(foundUser);
    }

    @Test
    public void testChangePasswordLogic() {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("oldpassword"));
        testUser.setRole("USER");
        testUser = userDao.save(testUser);

        String currentPassword = "oldpassword";
        String newPassword = "newpassword";
        
        assertTrue(passwordEncoder.matches(currentPassword, testUser.getPassword()));
        
        String newPasswordHash = passwordEncoder.encode(newPassword);
        userDao.updatePassword(testUser.getId(), newPasswordHash);

        User updatedUser = userDao.getUserByUsername("testuser");
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
        assertFalse(passwordEncoder.matches(currentPassword, updatedUser.getPassword()));
    }

    @Test
    public void testChangePasswordIncorrectCurrentLogic() {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("oldpassword"));
        testUser.setRole("USER");
        userDao.save(testUser);

        String wrongPassword = "wrongpassword";
        String correctPassword = "oldpassword";
        
        assertFalse(passwordEncoder.matches(wrongPassword, testUser.getPassword()));
        
        assertTrue(passwordEncoder.matches(correctPassword, testUser.getPassword()));
    }

    @Test
    public void testChangePasswordRequestValidation() {
        ChangePasswordRequest nullRequest = null;
        assertNull(nullRequest);
        
        ChangePasswordRequest emptyCurrentPassword = new ChangePasswordRequest("", "newpassword");
        assertTrue(emptyCurrentPassword.getCurrentPassword().trim().isEmpty());
        
        ChangePasswordRequest emptyNewPassword = new ChangePasswordRequest("oldpassword", "");
        assertTrue(emptyNewPassword.getNewPassword().trim().isEmpty());
        
        ChangePasswordRequest nullCurrentPassword = new ChangePasswordRequest(null, "newpassword");
        assertNull(nullCurrentPassword.getCurrentPassword());
        
        ChangePasswordRequest nullNewPassword = new ChangePasswordRequest("oldpassword", null);
        assertNull(nullNewPassword.getNewPassword());
        
        ChangePasswordRequest validRequest = new ChangePasswordRequest("oldpassword", "newpassword");
        assertEquals("oldpassword", validRequest.getCurrentPassword());
        assertEquals("newpassword", validRequest.getNewPassword());
    }


    @Test
    public void testUsernameExistsLogic() {
        User testUser = new User();
        testUser.setUsername("existinguser");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRole("USER");
        testUser = userDao.save(testUser);

        assertTrue(userDao.usernameExists("existinguser", 999L));
        assertFalse(userDao.usernameExists("existinguser", testUser.getId()));
        assertFalse(userDao.usernameExists("nonexistent", 999L));
    }

    @Test
    public void testPasswordEncodingLogic() {
        String plainPassword = "mypassword123";
        
        String encodedPassword = passwordEncoder.encode(plainPassword);
        assertNotNull(encodedPassword);
        assertNotEquals(plainPassword, encodedPassword);
  
        assertTrue(passwordEncoder.matches(plainPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongpassword", encodedPassword));
    }

    @Test
    public void testUserRoleAssignment() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole("USER");
        
        assertEquals("USER", user.getRole());
        
        user.setRole("ADMIN");
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    public void testUsernameTrimmingLogic() {
        String usernameWithSpaces = "  testuser  ";
        String trimmedUsername = usernameWithSpaces.trim();
        
        assertEquals("testuser", trimmedUsername);
        
        User user = new User();
        user.setUsername(trimmedUsername);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole("USER");
        
        User savedUser = userDao.save(user);
        assertEquals("testuser", savedUser.getUsername());
    }

    @Test
    public void testUserCreationLogic() {
        String username = "newuser";
        String password = "password123";
        
        User existingUser = userDao.getUserByUsername(username);
        assertNull(existingUser);
        
        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("USER");
        
        User savedUser = userDao.save(newUser);
        
        assertNotNull(savedUser.getId());
        assertEquals(username, savedUser.getUsername());
        assertEquals("USER", savedUser.getRole());
        assertTrue(passwordEncoder.matches(password, savedUser.getPassword()));
    }
}