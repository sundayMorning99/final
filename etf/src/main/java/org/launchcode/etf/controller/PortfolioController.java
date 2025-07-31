package org.launchcode.etf.controller;

import org.launchcode.etf.dao.PortfolioDao;
import org.launchcode.etf.dao.PortfolioEtfDao;
import org.launchcode.etf.dao.UserDao;
import org.launchcode.etf.dao.EtfDao;
import org.launchcode.etf.model.Portfolio;
import org.launchcode.etf.model.Etf;
import org.launchcode.etf.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")

public class PortfolioController {
    private final PortfolioDao portfolioDao;
    private final PortfolioEtfDao portfolioEtfDao;
    private final UserDao userDao;
    private final EtfDao etfDao;

    public PortfolioController(PortfolioDao portfolioDao, PortfolioEtfDao portfolioEtfDao, UserDao userDao, EtfDao etfDao) {
        this.portfolioDao = portfolioDao;
        this.portfolioEtfDao = portfolioEtfDao;
        this.userDao = userDao;
        this.etfDao = etfDao;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Portfolio> getAllPortfolios(@RequestParam(required = false) String search, @RequestParam(required = false) String sortBy, Principal principal) {
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (search != null && !search.trim().isEmpty()) {
            return portfolioDao.search(search, sortBy, user.getId(), isAdmin);
        }

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            return portfolioDao.findAllSorted(sortBy, user.getId(), isAdmin);
        }

        if (isAdmin) {
            return portfolioDao.findAll();
        } else {
            return portfolioDao.findByUserIdOrPublic(user.getId());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Portfolio getPortfolio(@PathVariable Long id, Principal principal) {
        Portfolio portfolio = portfolioDao.findById(id);
        if (portfolio == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found");
        }
        
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (!isAdmin && !portfolio.getUserId().equals(user.getId()) && !portfolio.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        return portfolio;
    }

    @GetMapping("/{id}/etfs")
    @PreAuthorize("isAuthenticated()")
    public List<Etf> getPortfolioEtfs(@PathVariable Long id, Principal principal) {
        Portfolio portfolio = portfolioDao.findById(id);
        if (portfolio == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found");
        }
        
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (!isAdmin && !portfolio.getUserId().equals(user.getId()) && !portfolio.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        return portfolioEtfDao.findEtfsByPortfolioId(id);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Portfolio createPortfolio(@RequestBody Portfolio portfolio, Principal principal) {
        User user = userDao.getUserByUsername(principal.getName());
        portfolio.setUserId(user.getId());
        portfolio.setId(null);
        return portfolioDao.save(portfolio);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Portfolio updatePortfolio(@PathVariable Long id, @RequestBody Portfolio portfolio, Principal principal) {
        Portfolio existingPortfolio = portfolioDao.findById(id);
        if (existingPortfolio == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found");
        }
        
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (!isAdmin && !existingPortfolio.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        portfolio.setId(id);
        portfolio.setUserId(existingPortfolio.getUserId());
        return portfolioDao.save(portfolio);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deletePortfolio(@PathVariable Long id, Principal principal) {
        Portfolio portfolio = portfolioDao.findById(id);
        if (portfolio == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found");
        }
        
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (!isAdmin && !portfolio.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        portfolioDao.deleteById(id);
    }

    @PostMapping("/{portfolioId}/etfs/{etfId}")
    @PreAuthorize("isAuthenticated()")
    public void addEtfToPortfolio(@PathVariable Long portfolioId, @PathVariable Long etfId, Principal principal) {
        Portfolio portfolio = portfolioDao.findById(portfolioId);
        if (portfolio == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found");
        }
        
        Etf etf = etfDao.findById(etfId);
        if (etf == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ETF not found");
        }
        
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (!isAdmin && !portfolio.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        if (portfolioEtfDao.existsByPortfolioIdAndEtfId(portfolioId, etfId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ETF already in portfolio");
        }
        
        portfolioEtfDao.addEtfToPortfolio(portfolioId, etfId);
    }

    @DeleteMapping("/{portfolioId}/etfs/{etfId}")
    @PreAuthorize("isAuthenticated()")
    public void removeEtfFromPortfolio(@PathVariable Long portfolioId, @PathVariable Long etfId, Principal principal) {
        Portfolio portfolio = portfolioDao.findById(portfolioId);
        if (portfolio == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found");
        }
        
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (!isAdmin && !portfolio.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        portfolioEtfDao.removeEtfFromPortfolio(portfolioId, etfId);
    }
}