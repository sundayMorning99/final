package org.launchcode.etf.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.launchcode.etf.model.Etf;
import org.launchcode.etf.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class EtfDaoTest {

    @Autowired
    private EtfDao etfDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Etf testEtf;
    private User testUser;

    @Before
    public void setUp() {
        // Clean up existing test data - Again, % represents one or more characters. 
        jdbcTemplate.update("DELETE FROM etf WHERE ticker LIKE 'TEST%'");
        jdbcTemplate.update("DELETE FROM users WHERE username LIKE 'test%'");
        
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("hashedpassword");
        testUser.setRole("USER");
        testUser = userDao.save(testUser);
     
        testEtf = new Etf();
        testEtf.setTicker("TESTVTI");
        testEtf.setDescription("Test Vanguard Total Stock Market ETF");
        testEtf.setAssetClass("Equity");
        testEtf.setExpenseRatio(new BigDecimal("0.03"));
        testEtf.setUserId(testUser.getId());
        testEtf.setIsPublic(true);
    }

    @Test
    public void testSaveNewEtf() {
        Etf savedEtf = etfDao.save(testEtf);
        
        assertNotNull(savedEtf.getId());
        assertEquals("TESTVTI", savedEtf.getTicker());
        assertEquals("Test Vanguard Total Stock Market ETF", savedEtf.getDescription());
        assertEquals("Equity", savedEtf.getAssetClass());
        assertEquals(new BigDecimal("0.03"), savedEtf.getExpenseRatio());
        assertEquals(testUser.getId(), savedEtf.getUserId());
        assertTrue(savedEtf.getIsPublic());
    }

    // updatedEtf will match only when set methods work properly. 
    @Test
    public void testUpdateExistingEtf() {
        Etf savedEtf = etfDao.save(testEtf);
        
        savedEtf.setDescription("Updated Test Description");
        savedEtf.setExpenseRatio(new BigDecimal("0.04"));
        savedEtf.setIsPublic(false);
        
        Etf updatedEtf = etfDao.save(savedEtf);
        
        assertEquals("Updated Test Description", updatedEtf.getDescription());
        assertEquals(new BigDecimal("0.04"), updatedEtf.getExpenseRatio());
        assertFalse(updatedEtf.getIsPublic());
    }

    // This works successfully only if getId() and findByid method works okay. 
    @Test
    public void testFindById() {
        Etf savedEtf = etfDao.save(testEtf);
        
        Etf foundEtf = etfDao.findById(savedEtf.getId());
        
        assertNotNull(foundEtf);
        assertEquals(savedEtf.getId(), foundEtf.getId());
        assertEquals("TESTVTI", foundEtf.getTicker());
    }

    @Test
    public void testFindByIdNotFound() {
        Etf foundEtf = etfDao.findById(99999L);
        assertNull(foundEtf);
    }

    @Test
    public void testFindAll() {
        etfDao.save(testEtf);
        
        Etf secondEtf = new Etf();
        secondEtf.setTicker("TESTQQQ");
        secondEtf.setDescription("Test NASDAQ ETF");
        secondEtf.setAssetClass("Equity");
        secondEtf.setExpenseRatio(new BigDecimal("0.20"));
        secondEtf.setUserId(testUser.getId());
        secondEtf.setIsPublic(true);
        etfDao.save(secondEtf);
        
        List<Etf> etfs = etfDao.findAll();
        
        assertTrue(etfs.size() >= 2);
        assertTrue(etfs.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
        assertTrue(etfs.stream().anyMatch(e -> "TESTQQQ".equals(e.getTicker())));
    }

    @Test
    public void testFindByUserId() {
        etfDao.save(testEtf);
        
        List<Etf> userEtfs = etfDao.findByUserId(testUser.getId());
        
        assertTrue(userEtfs.size() >= 1);
        assertTrue(userEtfs.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
    }

    @Test
    public void testFindPublicEtfs() {
        testEtf.setIsPublic(true);
        etfDao.save(testEtf);
        
        Etf privateEtf = new Etf();
        privateEtf.setTicker("TESTPRI");
        privateEtf.setDescription("Test Private ETF");
        privateEtf.setAssetClass("Bond");
        privateEtf.setExpenseRatio(new BigDecimal("0.10"));
        privateEtf.setUserId(testUser.getId());
        privateEtf.setIsPublic(false);
        etfDao.save(privateEtf);
        
        List<Etf> publicEtfs = etfDao.findPublicEtfs();
        
        assertTrue(publicEtfs.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
        assertFalse(publicEtfs.stream().anyMatch(e -> "TESTPRI".equals(e.getTicker())));
    }

    @Test
    public void testFindByUserIdOrPublic() {
        etfDao.save(testEtf);
        
        List<Etf> etfs = etfDao.findByUserIdOrPublic(testUser.getId());
        
        assertTrue(etfs.size() >= 1);
        assertTrue(etfs.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
    }

    @Test
    public void testSearchAsAdmin() {
        etfDao.save(testEtf);
        
        List<Etf> results = etfDao.search("VTI", null, testUser.getId(), true);
        
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
    }

    @Test
    public void testSearchAsUser() {
        etfDao.save(testEtf);
        
        List<Etf> results = etfDao.search("VTI", null, testUser.getId(), false);
        
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
    }

    @Test
    public void testSearchWithSorting() {
        testEtf.setAssetClass("Equity");
        etfDao.save(testEtf);
        
        Etf bondEtf = new Etf();
        bondEtf.setTicker("TESTBND");
        bondEtf.setDescription("Test Bond ETF");
        bondEtf.setAssetClass("Bond");
        bondEtf.setExpenseRatio(new BigDecimal("0.05"));
        bondEtf.setUserId(testUser.getId());
        bondEtf.setIsPublic(true);
        etfDao.save(bondEtf);
        
        List<Etf> results = etfDao.search("TEST", "assetClass", testUser.getId(), true);
        
        assertTrue(results.size() >= 2);
        // Should be sorted by asset class
        if (results.size() >= 2) {
            assertTrue(results.get(0).getAssetClass().compareTo(results.get(1).getAssetClass()) <= 0);
        }
    }

    @Test
    public void testFindAllSortedAsAdmin() {
        etfDao.save(testEtf);
        
        List<Etf> sortedEtfs = etfDao.findAllSorted("assetClass", testUser.getId(), true);
        
        assertTrue(sortedEtfs.size() >= 1);
    }

    @Test
    public void testFindAllSortedAsUser() {
        etfDao.save(testEtf);
        
        List<Etf> sortedEtfs = etfDao.findAllSorted("assetClass", testUser.getId(), false);
        
        assertTrue(sortedEtfs.size() >= 1);
    }

    @Test
    public void testDeleteById() {
        Etf savedEtf = etfDao.save(testEtf);
        Long etfId = savedEtf.getId();
        
        etfDao.deleteById(etfId);
        
        Etf foundEtf = etfDao.findById(etfId);
        assertNull(foundEtf);
    }
}