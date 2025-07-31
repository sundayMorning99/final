package org.launchcode.etf.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.launchcode.etf.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;

    @Before
    public void setUp() {
        // Clean up any existing test data (including admin test user)
        jdbcTemplate.update("DELETE FROM users WHERE username LIKE 'test%' OR username = 'admin'");
        
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("hashedpassword");
        testUser.setRole("USER");
    }

    @Test
    public void testSaveUser() {
        User savedUser = userDao.save(testUser);
        
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("hashedpassword", savedUser.getPassword());
        assertEquals("USER", savedUser.getRole());
    }

    @Test
    public void testFindByUsername() {
        userDao.save(testUser);
        
        User foundUser = userDao.findByUsername("testuser");
        
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("USER", foundUser.getRole());
    }

    @Test
    public void testFindByUsernameNotFound() {
        User foundUser = userDao.findByUsername("nonexistent");
        assertNull(foundUser);
    }

    @Test
    public void testGetUserByUsername() {
        userDao.save(testUser);
        
        User foundUser = userDao.getUserByUsername("testuser");
        
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    public void testFindById() {
        User savedUser = userDao.save(testUser);
        
        User foundUser = userDao.findById(savedUser.getId());
        
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    public void testFindAll() {
        userDao.save(testUser);
        
        User secondUser = new User();
        secondUser.setUsername("testuser2");
        secondUser.setPassword("hashedpassword2");
        secondUser.setRole("ADMIN");
        userDao.save(secondUser);
        
        List<User> users = userDao.findAll();
        
        assertTrue(users.size() >= 2);
        assertTrue(users.stream().anyMatch(u -> "testuser".equals(u.getUsername())));
        assertTrue(users.stream().anyMatch(u -> "testuser2".equals(u.getUsername())));
    }

    @Test
    public void testSearch() {
        userDao.save(testUser);
        
        List<User> results = userDao.search("test");
        
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().anyMatch(u -> "testuser".equals(u.getUsername())));
    }

    @Test
    public void testUpdate() {
        User savedUser = userDao.save(testUser);
        
        savedUser.setUsername("updateduser");
        savedUser.setRole("ADMIN");
        
        User updatedUser = userDao.update(savedUser);
        
        assertEquals("updateduser", updatedUser.getUsername());
        assertEquals("ADMIN", updatedUser.getRole());
        
        User foundUser = userDao.findById(savedUser.getId());
        assertEquals("updateduser", foundUser.getUsername());
        assertEquals("ADMIN", foundUser.getRole());
    }

    @Test
    public void testUpdatePassword() {
        User savedUser = userDao.save(testUser);
        
        userDao.updatePassword(savedUser.getId(), "newhashedpassword");
        
        User foundUser = userDao.findById(savedUser.getId());
        assertEquals("newhashedpassword", foundUser.getPassword());
    }

    @Test
    public void testDeleteById() {
        User savedUser = userDao.save(testUser);
        Long userId = savedUser.getId();
        
        userDao.deleteById(userId);
        
        User foundUser = userDao.findById(userId);
        assertNull(foundUser);
    }

    @Test
    public void testUsernameExists() {
        User savedUser = userDao.save(testUser);
        
        // Test with different user ID
        boolean exists = userDao.usernameExists("testuser", 999L);
        assertTrue(exists);
        
        // Test with same user ID (should return false)
        boolean existsWithSameId = userDao.usernameExists("testuser", savedUser.getId());
        assertFalse(existsWithSameId);
        
        // Test with non-existent username
        boolean notExists = userDao.usernameExists("nonexistent", 999L);
        assertFalse(notExists);
    }

    @Test
    public void testGetRoles() {
        userDao.save(testUser);
        
        List<String> roles = userDao.getRoles("testuser");
        
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    @Test(expected = RuntimeException.class)
    public void testGetRolesUserNotFound() {
        userDao.getRoles("nonexistent");
    }

    @Test
    public void testGetRolesAdmin() {
        User adminUser = new User();
        adminUser.setUsername("testadmin");
        adminUser.setPassword("hashedpassword");
        adminUser.setRole("ADMIN");
        userDao.save(adminUser);
        
        List<String> roles = userDao.getRoles("testadmin");
        
        assertEquals(1, roles.size());
        assertEquals("ROLE_ADMIN", roles.get(0));
    }

    @Test
    public void testFindByIdNotFound() {
        User foundUser = userDao.findById(99999L);
        assertNull(foundUser);
    }

    @Test
    public void testSearchNoResults() {
        List<User> results = userDao.search("nonexistentuser");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSaveUserWithLongUsername() {
        User longUsernameUser = new User();
        longUsernameUser.setUsername("verylongusernamethatmightcaussissues");
        longUsernameUser.setPassword("password");
        longUsernameUser.setRole("USER");
        
        User savedUser = userDao.save(longUsernameUser);
        assertNotNull(savedUser.getId());
        assertEquals("verylongusernamethatmightcaussissues", savedUser.getUsername());
    }

    @Test
    public void testMultipleUserRoles() {
        // Test USER role
        User regularUser = new User();
        regularUser.setUsername("testregular");
        regularUser.setPassword("password");
        regularUser.setRole("USER");
        userDao.save(regularUser);
        
        // Test ADMIN role
        User adminUser = new User();
        adminUser.setUsername("testadmin2");
        adminUser.setPassword("password");
        adminUser.setRole("ADMIN");
        userDao.save(adminUser);
        
        // Verify both users exist with correct roles
        User foundRegular = userDao.findByUsername("testregular");
        User foundAdmin = userDao.findByUsername("testadmin2");
        
        assertEquals("USER", foundRegular.getRole());
        assertEquals("ADMIN", foundAdmin.getRole());
        
        // Test role retrieval
        List<String> regularRoles = userDao.getRoles("testregular");
        List<String> adminRoles = userDao.getRoles("testadmin2");
        
        assertEquals("ROLE_USER", regularRoles.get(0));
        assertEquals("ROLE_ADMIN", adminRoles.get(0));
    }
}