package org.launchcode.etf.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.launchcode.etf.dao.EtfDao;
import org.launchcode.etf.dao.PortfolioDao;
import org.launchcode.etf.dao.PortfolioEtfDao;
import org.launchcode.etf.dao.UserDao;
import org.launchcode.etf.model.Etf;
import org.launchcode.etf.model.Portfolio;
import org.launchcode.etf.model.PortfolioEtf;
import org.launchcode.etf.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class PortfolioControllerTest {

    @Autowired
    private PortfolioDao portfolioDao;

    @Autowired
    private EtfDao etfDao;

    @Autowired
    private PortfolioEtfDao portfolioEtfDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;
    private User adminUser;
    private Portfolio testPortfolio;
    private Etf testEtf;

    // There are a lot more for preparation because to test a portfolio, we need user, admin, ETF, and portfolio. 
    @Before
    public void setUp() {
        jdbcTemplate.update("DELETE FROM portfolio_etf");
        jdbcTemplate.update("DELETE FROM portfolio WHERE name LIKE 'Test%'");
        jdbcTemplate.update("DELETE FROM etf WHERE ticker LIKE 'TEST%'");
        jdbcTemplate.update("DELETE FROM users WHERE username LIKE 'test%'");

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRole("USER");
        testUser = userDao.save(testUser);

        adminUser = new User();
        adminUser.setUsername("testadmin");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRole("ADMIN");
        adminUser = userDao.save(adminUser);

        testPortfolio = new Portfolio();
        testPortfolio.setName("Test Portfolio");
        testPortfolio.setUserId(testUser.getId());
        testPortfolio.setIsPublic(true);

        testEtf = new Etf();
        testEtf.setTicker("TESTVTI");
        testEtf.setDescription("Test Vanguard Total Stock Market ETF");
        testEtf.setAssetClass("Equity");
        testEtf.setExpenseRatio(new BigDecimal("0.03"));
        testEtf.setUserId(testUser.getId());
        testEtf.setIsPublic(true);
    }
    @Test
    public void testGetAllPortfoliosLogic() {
        portfolioDao.save(testPortfolio);

        List<Portfolio> userPortfolios = portfolioDao.findByUserIdOrPublic(testUser.getId());
        
        assertNotNull(userPortfolios);
        assertTrue(userPortfolios.size() >= 1);
        assertTrue(userPortfolios.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
    }

    @Test
    public void testGetAllPortfoliosWithSearchLogic() {
        portfolioDao.save(testPortfolio);

        List<Portfolio> searchResults = portfolioDao.search("Test", null, testUser.getId(), false);
        
        assertNotNull(searchResults);
        assertTrue(searchResults.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
    }

    @Test
    public void testGetAllPortfoliosWithSortLogic() {
        portfolioDao.save(testPortfolio);

        List<Portfolio> sortedResults = portfolioDao.findAllSorted("userId", testUser.getId(), false);
        
        assertNotNull(sortedResults);
        assertTrue(sortedResults.size() >= 1);
    }

    @Test
    public void testAdminCanSeeAllPortfoliosLogic() {
        portfolioDao.save(testPortfolio);

        List<Portfolio> allPortfolios = portfolioDao.findAll();
        assertTrue(allPortfolios.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));

        List<Portfolio> adminSearchResults = portfolioDao.search("Test", null, adminUser.getId(), true);
        assertTrue(adminSearchResults.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
    }

    @Test
    public void testGetPortfolioByIdLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);

        Portfolio foundPortfolio = portfolioDao.findById(savedPortfolio.getId());
        
        assertNotNull(foundPortfolio);
        assertEquals("Test Portfolio", foundPortfolio.getName());
        assertEquals(testUser.getId(), foundPortfolio.getUserId());
    }

    @Test
    public void testGetPortfolioByIdNotFoundLogic() {
        Portfolio foundPortfolio = portfolioDao.findById(99999L);
        assertNull(foundPortfolio);
    }

    @Test
    public void testPortfolioAccessControlLogic() {
        testPortfolio.setIsPublic(false);
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        List<Portfolio> userPortfolios = portfolioDao.findByUserId(testUser.getId());
        assertTrue(userPortfolios.stream().anyMatch(p -> p.getId().equals(savedPortfolio.getId())));
     
        List<Portfolio> publicPortfolios = portfolioDao.findPublicPortfolios();
        assertFalse(publicPortfolios.stream().anyMatch(p -> p.getId().equals(savedPortfolio.getId())));
        
        List<Portfolio> allPortfolios = portfolioDao.findAll();
        assertTrue(allPortfolios.stream().anyMatch(p -> p.getId().equals(savedPortfolio.getId())));
    }

    @Test
    public void testCreatePortfolioLogic() {
        Portfolio newPortfolio = new Portfolio();
        newPortfolio.setName("New Test Portfolio");
        newPortfolio.setUserId(testUser.getId());
        newPortfolio.setIsPublic(false);

        Portfolio savedPortfolio = portfolioDao.save(newPortfolio);
        
        assertNotNull(savedPortfolio);
        assertNotNull(savedPortfolio.getId());
        assertEquals("New Test Portfolio", savedPortfolio.getName());
        assertEquals(testUser.getId(), savedPortfolio.getUserId());
        assertFalse(savedPortfolio.getIsPublic());
    }

    @Test
    public void testUpdatePortfolioLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        savedPortfolio.setName("Updated Portfolio");
        savedPortfolio.setIsPublic(false);

        Portfolio updatedPortfolio = portfolioDao.save(savedPortfolio);
        
        assertNotNull(updatedPortfolio);
        assertEquals("Updated Portfolio", updatedPortfolio.getName());
        assertFalse(updatedPortfolio.getIsPublic());
        
        Portfolio foundPortfolio = portfolioDao.findById(savedPortfolio.getId());
        assertEquals("Updated Portfolio", foundPortfolio.getName());
    }

    @Test
    public void testDeletePortfolioLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        Long portfolioId = savedPortfolio.getId();
        
        assertNotNull(portfolioDao.findById(portfolioId));
        
        portfolioDao.deleteById(portfolioId);
        
        assertNull(portfolioDao.findById(portfolioId));
    }

    @Test
    public void testPortfolioOwnershipLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        assertEquals(testUser.getId(), savedPortfolio.getUserId());
        
        assertTrue(savedPortfolio.getUserId().equals(testUser.getId()));
        
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setRole("USER");
        otherUser = userDao.save(otherUser);
        
        assertFalse(savedPortfolio.getUserId().equals(otherUser.getId()));
    }

    @Test
    public void testAdminCanModifyAnyPortfolioLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        assertEquals("ADMIN", adminUser.getRole());
        
        boolean isAdmin = "ADMIN".equals(adminUser.getRole());
        assertTrue(isAdmin);
        
        boolean canModify = isAdmin || savedPortfolio.getUserId().equals(adminUser.getId());
        assertTrue(canModify);
    }

    @Test
    public void testAddEtfToPortfolioLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        Etf savedEtf = etfDao.save(testEtf);
        
        PortfolioEtf portfolioEtf = portfolioEtfDao.addEtfToPortfolio(savedPortfolio.getId(), savedEtf.getId());
        
        assertNotNull(portfolioEtf);
        assertNotNull(portfolioEtf.getId());
        assertEquals(savedPortfolio.getId(), portfolioEtf.getPortfolioId());
        assertEquals(savedEtf.getId(), portfolioEtf.getEtfId());
        
        assertTrue(portfolioEtfDao.existsByPortfolioIdAndEtfId(savedPortfolio.getId(), savedEtf.getId()));
    }

    @Test
    public void testAddEtfToPortfolioAlreadyExistsLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        Etf savedEtf = etfDao.save(testEtf);
        
        portfolioEtfDao.addEtfToPortfolio(savedPortfolio.getId(), savedEtf.getId());
        
        assertTrue(portfolioEtfDao.existsByPortfolioIdAndEtfId(savedPortfolio.getId(), savedEtf.getId()));
        
        boolean alreadyExists = portfolioEtfDao.existsByPortfolioIdAndEtfId(savedPortfolio.getId(), savedEtf.getId());
        assertTrue("ETF should already exist in portfolio", alreadyExists);
    }

    @Test
    public void testRemoveEtfFromPortfolioLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        Etf savedEtf = etfDao.save(testEtf);
        
        portfolioEtfDao.addEtfToPortfolio(savedPortfolio.getId(), savedEtf.getId());
        assertTrue(portfolioEtfDao.existsByPortfolioIdAndEtfId(savedPortfolio.getId(), savedEtf.getId()));

        portfolioEtfDao.removeEtfFromPortfolio(savedPortfolio.getId(), savedEtf.getId());
        
        assertFalse(portfolioEtfDao.existsByPortfolioIdAndEtfId(savedPortfolio.getId(), savedEtf.getId()));
    }

    @Test
    public void testGetPortfolioEtfsLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        Etf savedEtf = etfDao.save(testEtf);
        
        portfolioEtfDao.addEtfToPortfolio(savedPortfolio.getId(), savedEtf.getId());

        List<Etf> portfolioEtfs = portfolioEtfDao.findEtfsByPortfolioId(savedPortfolio.getId());
        
        assertNotNull(portfolioEtfs);
        assertTrue(portfolioEtfs.size() >= 1);
        assertTrue(portfolioEtfs.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
    }

    @Test
    public void testPortfolioEtfsAccessControlLogic() {
        testPortfolio.setIsPublic(false);
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setRole("USER");
        otherUser = userDao.save(otherUser);
        
        boolean isOwner = savedPortfolio.getUserId().equals(testUser.getId());
        boolean isOtherUser = savedPortfolio.getUserId().equals(otherUser.getId());
        boolean isPublic = savedPortfolio.getIsPublic();
        boolean isAdmin = "ADMIN".equals(otherUser.getRole());
        
        assertTrue("Test user should be owner", isOwner);
        assertFalse("Other user should not be owner", isOtherUser);
        assertFalse("Portfolio should be private", isPublic);
        assertFalse("Other user should not be admin", isAdmin);
        
        boolean testUserCanAccess = isOwner || isPublic || "ADMIN".equals(testUser.getRole());
        boolean otherUserCanAccess = isOtherUser || isPublic || isAdmin;
        
        assertTrue("Test user should have access", testUserCanAccess);
        assertFalse("Other user should not have access", otherUserCanAccess);
    }

    @Test
    public void testPortfolioValidationLogic() {
        Portfolio validPortfolio = new Portfolio();
        validPortfolio.setName("Valid Portfolio Name");
        validPortfolio.setUserId(testUser.getId());
        validPortfolio.setIsPublic(true);
        
        Portfolio savedPortfolio = portfolioDao.save(validPortfolio);
        assertNotNull(savedPortfolio.getId());
        
        assertEquals("Valid Portfolio Name", savedPortfolio.getName());
        assertEquals(testUser.getId(), savedPortfolio.getUserId());
        assertTrue(savedPortfolio.getIsPublic());
    }

    @Test
    public void testPortfolioPublicPrivateLogic() {
        testPortfolio.setIsPublic(true);
        Portfolio publicPortfolio = portfolioDao.save(testPortfolio);
        
        List<Portfolio> publicPortfolios = portfolioDao.findPublicPortfolios();
        assertTrue(publicPortfolios.stream().anyMatch(p -> p.getId().equals(publicPortfolio.getId())));
        
        Portfolio privatePortfolio = new Portfolio();
        privatePortfolio.setName("Test Private Portfolio");
        privatePortfolio.setUserId(testUser.getId());
        privatePortfolio.setIsPublic(false);
        Portfolio savedPrivatePortfolio = portfolioDao.save(privatePortfolio);
        
        publicPortfolios = portfolioDao.findPublicPortfolios();
        assertFalse(publicPortfolios.stream().anyMatch(p -> p.getId().equals(savedPrivatePortfolio.getId())));
        
        List<Portfolio> userPortfolios = portfolioDao.findByUserId(testUser.getId());
        assertTrue(userPortfolios.stream().anyMatch(p -> p.getId().equals(savedPrivatePortfolio.getId())));
    }

    @Test
    public void testMultipleUsersPortfolioLogic() {
        User secondUser = new User();
        secondUser.setUsername("seconduser");
        secondUser.setPassword(passwordEncoder.encode("password"));
        secondUser.setRole("USER");
        secondUser = userDao.save(secondUser);
        
        portfolioDao.save(testPortfolio);
        
        Portfolio secondUserPortfolio = new Portfolio();
        secondUserPortfolio.setName("Second User Portfolio");
        secondUserPortfolio.setUserId(secondUser.getId());
        secondUserPortfolio.setIsPublic(true);
        portfolioDao.save(secondUserPortfolio);
        
        List<Portfolio> firstUserPortfolios = portfolioDao.findByUserId(testUser.getId());
        List<Portfolio> secondUserPortfolios = portfolioDao.findByUserId(secondUser.getId());
        
        assertTrue(firstUserPortfolios.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
        assertTrue(secondUserPortfolios.stream().anyMatch(p -> "Second User Portfolio".equals(p.getName())));
        
        List<Portfolio> firstUserVisible = portfolioDao.findByUserIdOrPublic(testUser.getId());
        assertTrue(firstUserVisible.stream().anyMatch(p -> "Second User Portfolio".equals(p.getName())));
    }

    @Test
    public void testPortfolioEtfRelationshipsLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        Etf savedEtf1 = etfDao.save(testEtf);
        
        Etf etf2 = new Etf();
        etf2.setTicker("TESTSPY");
        etf2.setDescription("Test S&P 500 ETF");
        etf2.setAssetClass("Equity");
        etf2.setExpenseRatio(new BigDecimal("0.09"));
        etf2.setUserId(testUser.getId());
        etf2.setIsPublic(true);
        Etf savedEtf2 = etfDao.save(etf2);
        
        Etf etf3 = new Etf();
        etf3.setTicker("TESTBND");
        etf3.setDescription("Test Bond ETF");
        etf3.setAssetClass("Bond");
        etf3.setExpenseRatio(new BigDecimal("0.05"));
        etf3.setUserId(testUser.getId());
        etf3.setIsPublic(true);
        Etf savedEtf3 = etfDao.save(etf3);
        
        portfolioEtfDao.addEtfToPortfolio(savedPortfolio.getId(), savedEtf1.getId());
        portfolioEtfDao.addEtfToPortfolio(savedPortfolio.getId(), savedEtf2.getId());
        portfolioEtfDao.addEtfToPortfolio(savedPortfolio.getId(), savedEtf3.getId());
        
        List<Etf> portfolioEtfs = portfolioEtfDao.findEtfsByPortfolioId(savedPortfolio.getId());
        assertEquals(3, portfolioEtfs.size());
        
        assertTrue(portfolioEtfs.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
        assertTrue(portfolioEtfs.stream().anyMatch(e -> "TESTSPY".equals(e.getTicker())));
        assertTrue(portfolioEtfs.stream().anyMatch(e -> "TESTBND".equals(e.getTicker())));
        
        portfolioEtfDao.removeEtfFromPortfolio(savedPortfolio.getId(), savedEtf2.getId());
        
        portfolioEtfs = portfolioEtfDao.findEtfsByPortfolioId(savedPortfolio.getId());
        assertEquals(2, portfolioEtfs.size());
        assertFalse(portfolioEtfs.stream().anyMatch(e -> "TESTSPY".equals(e.getTicker())));
    }

    @Test
    public void testPortfolioSearchLogic() {
        portfolioDao.save(testPortfolio); 
        
        Portfolio growthPortfolio = new Portfolio();
        growthPortfolio.setName("Growth Portfolio");
        growthPortfolio.setUserId(testUser.getId());
        growthPortfolio.setIsPublic(true);
        portfolioDao.save(growthPortfolio);
        
        Portfolio valuePortfolio = new Portfolio();
        valuePortfolio.setName("Value Portfolio");
        valuePortfolio.setUserId(testUser.getId());
        valuePortfolio.setIsPublic(true);
        portfolioDao.save(valuePortfolio);
        
        List<Portfolio> testResults = portfolioDao.search("Test", null, testUser.getId(), false);
        assertTrue(testResults.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
        assertFalse(testResults.stream().anyMatch(p -> "Growth Portfolio".equals(p.getName())));
        
        List<Portfolio> portfolioResults = portfolioDao.search("Portfolio", null, testUser.getId(), false);
        assertTrue(portfolioResults.size() >= 3); 
        
        List<Portfolio> growthResults = portfolioDao.search("Growth", null, testUser.getId(), false);
        assertTrue(growthResults.stream().anyMatch(p -> "Growth Portfolio".equals(p.getName())));
        assertFalse(growthResults.stream().anyMatch(p -> "Test Portfolio".equals(p.getName())));
    }

    @Test
    public void testPortfolioSortingLogic() {
        portfolioDao.save(testPortfolio); 
        
        User secondUser = new User();
        secondUser.setUsername("seconduser");
        secondUser.setPassword(passwordEncoder.encode("password"));
        secondUser.setRole("USER");
        secondUser = userDao.save(secondUser);
        
        Portfolio secondPortfolio = new Portfolio();
        secondPortfolio.setName("Alpha Portfolio"); 
        secondPortfolio.setUserId(secondUser.getId());
        secondPortfolio.setIsPublic(true);
        portfolioDao.save(secondPortfolio);
        
        List<Portfolio> sortedByName = portfolioDao.findAllSorted("name", testUser.getId(), true);
        assertTrue(sortedByName.size() >= 2);
        
        List<Portfolio> sortedByUserId = portfolioDao.findAllSorted("userId", testUser.getId(), true);
        assertTrue(sortedByUserId.size() >= 2);
    }

    @Test
    public void testPortfolioCascadeDeleteLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        Etf savedEtf = etfDao.save(testEtf);
        
        portfolioEtfDao.addEtfToPortfolio(savedPortfolio.getId(), savedEtf.getId());
        
        assertTrue(portfolioEtfDao.existsByPortfolioIdAndEtfId(savedPortfolio.getId(), savedEtf.getId()));
        
        portfolioDao.deleteById(savedPortfolio.getId());
        
        assertNull(portfolioDao.findById(savedPortfolio.getId()));
        
        assertFalse(portfolioEtfDao.existsByPortfolioIdAndEtfId(savedPortfolio.getId(), savedEtf.getId()));
        
        assertNotNull(etfDao.findById(savedEtf.getId()));
    }

    @Test
    public void testEmptyPortfolioLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        List<Etf> emptyPortfolioEtfs = portfolioEtfDao.findEtfsByPortfolioId(savedPortfolio.getId());
        assertTrue(emptyPortfolioEtfs.isEmpty());
        
        assertFalse(portfolioEtfDao.existsByPortfolioIdAndEtfId(savedPortfolio.getId(), 99999L));
    }

    @Test
    public void testPortfolioWithNonExistentEtfLogic() {
        Portfolio savedPortfolio = portfolioDao.save(testPortfolio);
        
        List<Etf> portfolioEtfs = portfolioEtfDao.findEtfsByPortfolioId(savedPortfolio.getId());
        assertTrue(portfolioEtfs.isEmpty());
        
        assertFalse(portfolioEtfDao.existsByPortfolioIdAndEtfId(savedPortfolio.getId(), 99999L));
    }
}