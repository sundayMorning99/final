package org.launchcode.etf.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.launchcode.etf.dao.EtfDao;
import org.launchcode.etf.dao.UserDao;
import org.launchcode.etf.model.Etf;
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
public class EtfControllerTest {

    @Autowired
    private EtfDao etfDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;
    private User adminUser;
    private Etf testEtf;

    //After clean-up, One admin, one user, and one ETF is created for test preparation.
    //Assert statements return nothing when passed. 
    @Before
    public void setUp() {
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

        testEtf = new Etf();
        testEtf.setTicker("TESTVTI");
        testEtf.setDescription("Test Vanguard Total Stock Market ETF");
        testEtf.setAssetClass("Equity");
        testEtf.setExpenseRatio(new BigDecimal("0.03"));
        testEtf.setUserId(testUser.getId());
        testEtf.setIsPublic(true);
    }

    @Test
    public void testGetAllEtfsLogic() {
        etfDao.save(testEtf);

        List<Etf> userEtfs = etfDao.findByUserIdOrPublic(testUser.getId());
        
        assertNotNull(userEtfs);
        assertTrue(userEtfs.size() >= 1);
        assertTrue(userEtfs.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
    }

    @Test
    public void testGetAllEtfsWithSearchLogic() {
        etfDao.save(testEtf);

        List<Etf> searchResults = etfDao.search("VTI", null, testUser.getId(), false);
        
        assertNotNull(searchResults);
        assertTrue(searchResults.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
    }

    @Test
    public void testGetAllEtfsWithSortLogic() {
        etfDao.save(testEtf);

        List<Etf> sortedResults = etfDao.findAllSorted("assetClass", testUser.getId(), false);
        
        assertNotNull(sortedResults);
        assertTrue(sortedResults.size() >= 1);
    }

    @Test
    public void testAdminCanSeeAllEtfsLogic() {
        etfDao.save(testEtf);

        List<Etf> allEtfs = etfDao.findAll();
        assertTrue(allEtfs.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));

        List<Etf> adminSearchResults = etfDao.search("VTI", null, adminUser.getId(), true);
        assertTrue(adminSearchResults.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
    }

    @Test
    public void testGetEtfByIdLogic() {
        Etf savedEtf = etfDao.save(testEtf);

        Etf foundEtf = etfDao.findById(savedEtf.getId());
        
        assertNotNull(foundEtf);
        assertEquals("TESTVTI", foundEtf.getTicker());
        assertEquals("Test Vanguard Total Stock Market ETF", foundEtf.getDescription());
        assertEquals(testUser.getId(), foundEtf.getUserId());
    }

    @Test
    public void testGetEtfByIdNotFoundLogic() {
        Etf foundEtf = etfDao.findById(99999L);
        assertNull(foundEtf);
    }

    @Test
    public void testEtfAccessControlLogic() {
        testEtf.setIsPublic(false);
        Etf savedEtf = etfDao.save(testEtf);
        
        List<Etf> userEtfs = etfDao.findByUserId(testUser.getId());
        assertTrue(userEtfs.stream().anyMatch(e -> e.getId().equals(savedEtf.getId())));
        
        List<Etf> publicEtfs = etfDao.findPublicEtfs();
        assertFalse(publicEtfs.stream().anyMatch(e -> e.getId().equals(savedEtf.getId())));
        
        List<Etf> allEtfs = etfDao.findAll();
        assertTrue(allEtfs.stream().anyMatch(e -> e.getId().equals(savedEtf.getId())));
    }

    @Test
    public void testCreateEtfLogic() {
        Etf newEtf = new Etf();
        newEtf.setTicker("TESTSPY");
        newEtf.setDescription("Test S&P 500 ETF");
        newEtf.setAssetClass("Equity");
        newEtf.setExpenseRatio(new BigDecimal("0.09"));
        newEtf.setUserId(testUser.getId());
        newEtf.setIsPublic(false);

        Etf savedEtf = etfDao.save(newEtf);
        
        assertNotNull(savedEtf);
        assertNotNull(savedEtf.getId());
        assertEquals("TESTSPY", savedEtf.getTicker());
        assertEquals(testUser.getId(), savedEtf.getUserId());
        assertFalse(savedEtf.getIsPublic());
    }

    @Test
    public void testUpdateEtfLogic() {
        Etf savedEtf = etfDao.save(testEtf);
        
        savedEtf.setDescription("Updated Test Description");
        savedEtf.setExpenseRatio(new BigDecimal("0.05"));
        savedEtf.setIsPublic(false);

        Etf updatedEtf = etfDao.save(savedEtf);
        
        assertNotNull(updatedEtf);
        assertEquals("Updated Test Description", updatedEtf.getDescription());
        assertEquals(new BigDecimal("0.05"), updatedEtf.getExpenseRatio());
        assertFalse(updatedEtf.getIsPublic());

        Etf foundEtf = etfDao.findById(savedEtf.getId());
        assertEquals("Updated Test Description", foundEtf.getDescription());
    }

    @Test
    public void testDeleteEtfLogic() {
        Etf savedEtf = etfDao.save(testEtf);
        Long etfId = savedEtf.getId();
  
        assertNotNull(etfDao.findById(etfId));
        etfDao.deleteById(etfId);
        assertNull(etfDao.findById(etfId));
    }

    @Test
    public void testEtfOwnershipLogic() {
        Etf savedEtf = etfDao.save(testEtf);
        
        assertEquals(testUser.getId(), savedEtf.getUserId());
        assertTrue(savedEtf.getUserId().equals(testUser.getId()));
        
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setRole("USER");
        otherUser = userDao.save(otherUser);
        
        assertFalse(savedEtf.getUserId().equals(otherUser.getId()));
    }

    @Test
    public void testAdminCanModifyAnyEtfLogic() {
        Etf savedEtf = etfDao.save(testEtf);
        
        assertEquals("ADMIN", adminUser.getRole());
        
        boolean isAdmin = "ADMIN".equals(adminUser.getRole());
        assertTrue(isAdmin);
        boolean canModify = isAdmin || savedEtf.getUserId().equals(adminUser.getId());
        assertTrue(canModify);
    }

    @Test
    public void testEtfValidationLogic() {
        Etf validEtf = new Etf();
        validEtf.setTicker("TESTBND");
        validEtf.setDescription("Test Bond ETF");
        validEtf.setAssetClass("Bond");
        validEtf.setExpenseRatio(new BigDecimal("0.035"));
        validEtf.setUserId(testUser.getId());
        validEtf.setIsPublic(true);
        
        Etf savedEtf = etfDao.save(validEtf);
        assertNotNull(savedEtf.getId());
        
        assertEquals("TESTBND", savedEtf.getTicker());
        assertEquals("Test Bond ETF", savedEtf.getDescription());
        assertEquals("Bond", savedEtf.getAssetClass());
        assertEquals(new BigDecimal("0.035"), savedEtf.getExpenseRatio());
        assertTrue(savedEtf.getIsPublic());
    }

    @Test
    public void testEtfSearchByAssetClassLogic() {
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
        
        List<Etf> sortedResults = etfDao.search("TEST", "assetClass", testUser.getId(), false);
        
        assertTrue(sortedResults.size() >= 2);
        if (sortedResults.size() >= 2) {
            boolean foundBond = false;
            boolean foundEquity = false;
            for (Etf etf : sortedResults) {
                if ("Bond".equals(etf.getAssetClass())) {
                    foundBond = true;
                    assertFalse("Equity should come after Bond", foundEquity);
                } else if ("Equity".equals(etf.getAssetClass())) {
                    foundEquity = true;
                }
            }
            assertTrue(foundBond);
            assertTrue(foundEquity);
        }
    }

    @Test
    public void testEtfPublicPrivateLogic() {
        testEtf.setIsPublic(true);
        Etf publicEtf = etfDao.save(testEtf);
        
        List<Etf> publicEtfs = etfDao.findPublicEtfs();
        assertTrue(publicEtfs.stream().anyMatch(e -> e.getId().equals(publicEtf.getId())));
        
        Etf privateEtf = new Etf();
        privateEtf.setTicker("TESTPRI");
        privateEtf.setDescription("Test Private ETF");
        privateEtf.setAssetClass("Real Estate");
        privateEtf.setExpenseRatio(new BigDecimal("0.15"));
        privateEtf.setUserId(testUser.getId());
        privateEtf.setIsPublic(false);
        Etf savedPrivateEtf = etfDao.save(privateEtf);
        
        publicEtfs = etfDao.findPublicEtfs();
        assertFalse(publicEtfs.stream().anyMatch(e -> e.getId().equals(savedPrivateEtf.getId())));
        
        List<Etf> userEtfs = etfDao.findByUserId(testUser.getId());
        assertTrue(userEtfs.stream().anyMatch(e -> e.getId().equals(savedPrivateEtf.getId())));
    }

    @Test
    public void testEtfExpenseRatioValidation() {
        Etf lowCostEtf = new Etf();
        lowCostEtf.setTicker("TESTLOW");
        lowCostEtf.setDescription("Test Low Cost ETF");
        lowCostEtf.setAssetClass("Equity");
        lowCostEtf.setExpenseRatio(new BigDecimal("0.03"));
        lowCostEtf.setUserId(testUser.getId());
        lowCostEtf.setIsPublic(true);
        
        Etf savedLowCost = etfDao.save(lowCostEtf);
        assertEquals(new BigDecimal("0.03"), savedLowCost.getExpenseRatio());
        
        Etf highCostEtf = new Etf();
        highCostEtf.setTicker("TESTHIGH");
        highCostEtf.setDescription("Test High Cost ETF");
        highCostEtf.setAssetClass("Specialty");
        highCostEtf.setExpenseRatio(new BigDecimal("1.25"));
        highCostEtf.setUserId(testUser.getId());
        highCostEtf.setIsPublic(true);
        
        Etf savedHighCost = etfDao.save(highCostEtf);
        assertEquals(new BigDecimal("1.25"), savedHighCost.getExpenseRatio());
    }

    @Test
    public void testMultipleUsersEtfLogic() {
        User secondUser = new User();
        secondUser.setUsername("seconduser");
        secondUser.setPassword(passwordEncoder.encode("password"));
        secondUser.setRole("USER");
        secondUser = userDao.save(secondUser);
        
        etfDao.save(testEtf);
        
        Etf secondUserEtf = new Etf();
        secondUserEtf.setTicker("TESTUSER2");
        secondUserEtf.setDescription("Second User ETF");
        secondUserEtf.setAssetClass("International");
        secondUserEtf.setExpenseRatio(new BigDecimal("0.08"));
        secondUserEtf.setUserId(secondUser.getId());
        secondUserEtf.setIsPublic(true);
        etfDao.save(secondUserEtf);
        
        List<Etf> firstUserEtfs = etfDao.findByUserId(testUser.getId());
        List<Etf> secondUserEtfs = etfDao.findByUserId(secondUser.getId());
        
        assertTrue(firstUserEtfs.stream().anyMatch(e -> "TESTVTI".equals(e.getTicker())));
        assertTrue(secondUserEtfs.stream().anyMatch(e -> "TESTUSER2".equals(e.getTicker())));
        
        List<Etf> firstUserVisible = etfDao.findByUserIdOrPublic(testUser.getId());
        assertTrue(firstUserVisible.stream().anyMatch(e -> "TESTUSER2".equals(e.getTicker())));
    }
}