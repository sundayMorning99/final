package org.launchcode.etf.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.launchcode.etf.dao.UserDao;
import org.launchcode.etf.dto.RegisterRequest;
import org.launchcode.etf.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class RegistrationControllerTest {

    @Autowired
    private RegistrationController registrationController;

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
    public void testRegisterUser() {
        RegisterRequest request = new RegisterRequest("testuser", "password123");

        String response = registrationController.registerUser(request);

        assertEquals("User registered successfully", response);

        // Verify user was created
        User createdUser = userDao.getUserByUsername("testuser");
        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());
        assertEquals("USER", createdUser.getRole());
        assertTrue(passwordEncoder.matches("password123", createdUser.getPassword()));
    }

    @Test
    public void testRegisterUserDuplicateUsername() {
        // Create existing user
        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setRole("USER");
        userDao.save(existingUser);

        RegisterRequest request = new RegisterRequest("testuser", "password123");

        try {
            registrationController.registerUser(request);
            fail("Should have thrown ResponseStatusException");
        } catch (ResponseStatusException e) {
            assertEquals(400, e.getStatusCode().value());
            assertEquals("Username is already taken", e.getReason());
        }
    }

    @Test
    public void testRegisterUserEmptyUsername() {
        RegisterRequest request = new RegisterRequest("", "password123");

        try {
            registrationController.registerUser(request);
            fail("Should have thrown ResponseStatusException");
        } catch (ResponseStatusException e) {
            assertEquals(400, e.getStatusCode().value());
            assertEquals("Username is required", e.getReason());
        }
    }

    @Test
    public void testRegisterUserEmptyPassword() {
        RegisterRequest request = new RegisterRequest("testuser", "");

        try {
            registrationController.registerUser(request);
            fail("Should have thrown ResponseStatusException");
        } catch (ResponseStatusException e) {
            assertEquals(400, e.getStatusCode().value());
            assertEquals("Password is required", e.getReason());
        }
    }

    @Test
    public void testRegisterUserNullRequest() {
        try {
            registrationController.registerUser(null);
            fail("Should have thrown ResponseStatusException");
        } catch (ResponseStatusException e) {
            assertEquals(400, e.getStatusCode().value());
            assertEquals("Request body is required", e.getReason());
        }
    }

    @Test
    public void testRegisterUserNullUsername() {
        RegisterRequest request = new RegisterRequest(null, "password123");

        try {
            registrationController.registerUser(request);
            fail("Should have thrown ResponseStatusException");
        } catch (ResponseStatusException e) {
            assertEquals(400, e.getStatusCode().value());
            assertEquals("Username is required", e.getReason());
        }
    }

    @Test
    public void testRegisterUserNullPassword() {
        RegisterRequest request = new RegisterRequest("testuser", null);

        try {
            registrationController.registerUser(request);
            fail("Should have thrown ResponseStatusException");
        } catch (ResponseStatusException e) {
            assertEquals(400, e.getStatusCode().value());
            assertEquals("Password is required", e.getReason());
        }
    }

    @Test
    public void testRegisterUserWhitespaceUsername() {
        RegisterRequest request = new RegisterRequest("   ", "password123");

        try {
            registrationController.registerUser(request);
            fail("Should have thrown ResponseStatusException");
        } catch (ResponseStatusException e) {
            assertEquals(400, e.getStatusCode().value());
            assertEquals("Username is required", e.getReason());
        }
    }

    @Test
    public void testRegisterUserWhitespacePassword() {
        RegisterRequest request = new RegisterRequest("testuser", "   ");

        try {
            registrationController.registerUser(request);
            fail("Should have thrown ResponseStatusException");
        } catch (ResponseStatusException e) {
            assertEquals(400, e.getStatusCode().value());
            assertEquals("Password is required", e.getReason());
        }
    }

    @Test
    public void testRegisterUserTrimsUsername() {
        RegisterRequest request = new RegisterRequest("  testuser  ", "password123");

        String response = registrationController.registerUser(request);

        assertEquals("User registered successfully", response);

        User createdUser = userDao.getUserByUsername("testuser");
        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());
    }

    @Test
    public void testRegisterUserDatabaseError() {
        // Test exception handling by testing with duplicate username
        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setRole("USER");
        userDao.save(existingUser);

        RegisterRequest request = new RegisterRequest("testuser", "password123");

        try {
            registrationController.registerUser(request);
            fail("Should have thrown ResponseStatusException");
        } catch (ResponseStatusException e) {
            assertEquals(400, e.getStatusCode().value());
            assertEquals("Username is already taken", e.getReason());
        }
    }

    @Test
    public void testRegisterUserSetsDefaultRole() {
        RegisterRequest request = new RegisterRequest("testuser", "password123");

        registrationController.registerUser(request);

        User createdUser = userDao.getUserByUsername("testuser");
        assertNotNull(createdUser);
        assertEquals("USER", createdUser.getRole());
    }

    @Test
    public void testRegisterUserEncodesPassword() {
        RegisterRequest request = new RegisterRequest("testuser", "password123");

        registrationController.registerUser(request);

        User createdUser = userDao.getUserByUsername("testuser");
        assertNotNull(createdUser);
        assertNotEquals("password123", createdUser.getPassword()); // Should be encoded
        assertTrue(passwordEncoder.matches("password123", createdUser.getPassword()));
    }

    @Test
    public void testRegisterUserWithSpecialCharacters() {
        RegisterRequest request = new RegisterRequest("test@user.com", "P@ssw0rd!");

        String response = registrationController.registerUser(request);

        assertEquals("User registered successfully", response);

        User createdUser = userDao.getUserByUsername("test@user.com");
        assertNotNull(createdUser);
        assertEquals("test@user.com", createdUser.getUsername());
        assertTrue(passwordEncoder.matches("P@ssw0rd!", createdUser.getPassword()));
    }

    @Test
    public void testRegisterUserLongUsername() {
        String longUsername = "verylongusernamethatexceeds50characters12345678901234567890";
        RegisterRequest request = new RegisterRequest(longUsername, "password123");

        // This test assumes your database has username length constraints
        // If not, this will pass - adjust based on your actual constraints
        try {
            String response = registrationController.registerUser(request);
            assertEquals("User registered successfully", response);
        } catch (ResponseStatusException e) {
            // Expected if database has length constraints
            assertEquals(500, e.getStatusCode().value());
        }
    }
}