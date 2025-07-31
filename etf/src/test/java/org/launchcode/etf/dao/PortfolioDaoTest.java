package org.launchcode.etf.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.launchcode.etf.model.Portfolio;
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
public class PortfolioDaoTest {

    @Autowired
    private PortfolioDao portfolioDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Portfolio testPortfolio;
    private User testUser;

    @Before
    public void setUp() {
        // Clean up existing test data
        jdbcTemplate.update("DELETE FROM portfolio WHERE name LIKE 'Test%'");
        jdbcTemplate.update("DELETE FROM users WHERE username LIKE 'test%'");
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("hashedpassword");
        testUser.setRole("USER");
        testUser = userDao.save(testUser);
        
        // Create test portfolio
        testPortfolio = new Portfolio();
        testPortfolio.setName("Test Portfolio");
        testPortfolio.setUserId(testUser.getId());
        testPortfolio.setIsPublic(true);
    }

    @Test
    public void testSaveNewPortfolio() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        assertNotNull(savedPortfolio.getId());
        assertEquals("Test Portfolio", savedPortfolio.getName());
        assertEquals(testUser.getId(), savedPortfolio.getUserId());
        assertTrue(savedPortfolio.getIsPublic());
    }

    @Test
    public void testUpdateExistingPortfolio() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        savedPortfolio.setName("Updated Portfolio");
        savedPortfolio.setIsPublic(false);
        
        Portfolio updatedPortfolio = portfolioDao.save(savedPortfolio);
        
        assertEquals("Updated Portfolio", updatedPortfolio.getName());
        assertFalse(updatedPortfolio.getIsPublic());
    }

    @Test
    public void testFindById() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        Portfolio foundPortfolio = portfolioDao.findById(savedPortfolio.getId());
        
        assertNotNull(foundPortfolio);
        assertEquals(savedPortfolio.getId(), foundPortfolio.getId());
        assertEquals("Test Portfolio", foundPortfolio.getName());
    }

    @Test
    public void testFindByIdNotFound() {
        Portfolio foundPortfolio = portfolioDao.findById(99999L);
        assertNull(foundPortfolio);
    }

    @Test
    public void testFindAll() {
        portfolioDao.save(testPortfolio);
        
        Portfolio secondPortfolio = new Portfolio();
        secondPortfolio.setName("Test Portfolio 2");
        secondPortfolio.setUserId(testUser.getId());
        secondPortfolio.setIsPublic(false);
        portfolioDao.save(secondPortfolio);
        
        List<Portfolio> portfolios = portfolioDao.findAll();
        
        assertTrue(portfolios.size() >= 2);
        assertTrue(portfolios.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
        assertTrue(portfolios.stream().anyMatch(p -> "Test Portfolio 2".equals(p.getName())));
    }

    @Test
    public void testFindByUserId() {
        portfolioDao.save(testPortfolio);
        
        List<Portfolio> userPortfolios = portfolioDao.findByUserId(testUser.getId());
        
        assertTrue(userPortfolios.size() >= 1);
        assertTrue(userPortfolios.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
    }

    @Test
    public void testFindPublicPortfolios() {
        testPortfolio.setIsPublic(true);
        portfolioDao.save(testPortfolio);
        
        Portfolio privatePortfolio = new Portfolio();
        privatePortfolio.setName("Test Private Portfolio");
        privatePortfolio.setUserId(testUser.getId());
        privatePortfolio.setIsPublic(false);
        portfolioDao.save(privatePortfolio);
        
        List<Portfolio> publicPortfolios = portfolioDao.findPublicPortfolios();
        
        assertTrue(publicPortfolios.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
        assertFalse(publicPortfolios.stream().anyMatch(p -> "Test Private Portfolio".equals(p.getName())));
    }

    @Test
    public void testFindByUserIdOrPublic() {
        portfolioDao.save(testPortfolio);
        
        List<Portfolio> portfolios = portfolioDao.findByUserIdOrPublic(testUser.getId());
        
        assertTrue(portfolios.size() >= 1);
        assertTrue(portfolios.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
    }

    @Test
    public void testSearch() {
        portfolioDao.save(testPortfolio);
        
        List<Portfolio> results = portfolioDao.search("Test", null, testUser.getId(), true);
        
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
    }

    @Test
    public void testFindAllSorted() {
        portfolioDao.save(testPortfolio);
        
        List<Portfolio> sortedPortfolios = portfolioDao.findAllSorted("userId", testUser.getId(), true);
        
        assertTrue(sortedPortfolios.size() >= 1);
    }

    @Test
    public void testDeleteById() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        Long portfolioId = savedPortfolio.getId();
        
        portfolioDao.deleteById(portfolioId);
        
        Portfolio foundPortfolio = portfolioDao.findById(portfolioId);
        assertNull(foundPortfolio);
    }
}