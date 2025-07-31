package org.launchcode.etf.controller;

import org.launchcode.etf.dao.EtfDao;
import org.launchcode.etf.dao.UserDao;
import org.launchcode.etf.model.Etf;
import org.launchcode.etf.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/etfs")

public class EtfController {
    private final EtfDao etfDao;
    private final UserDao userDao;

    public EtfController(EtfDao etfDao, UserDao userDao) {
        this.etfDao = etfDao;
        this.userDao = userDao;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Etf> getAllEtfs(@RequestParam(required = false) String search, @RequestParam(required = false) String sortBy, Principal principal) {
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (search != null && !search.trim().isEmpty()) {
            return etfDao.search(search, sortBy, user.getId(), isAdmin);
        }

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            return etfDao.findAllSorted(sortBy, user.getId(), isAdmin);
        }

        if (isAdmin) {
            return etfDao.findAll();
        } else {
            return etfDao.findByUserIdOrPublic(user.getId());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Etf getEtf(@PathVariable Long id, Principal principal) {
        Etf etf = etfDao.findById(id);
        if (etf == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ETF not found");
        }
        
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (!isAdmin && !etf.getUserId().equals(user.getId()) && !etf.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        return etf;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Etf createEtf(@RequestBody Etf etf, Principal principal) {
        User user = userDao.getUserByUsername(principal.getName());
        etf.setUserId(user.getId());
        etf.setId(null);
        return etfDao.save(etf);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Etf updateEtf(@PathVariable Long id, @RequestBody Etf etf, Principal principal) {
        Etf existingEtf = etfDao.findById(id);
        if (existingEtf == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ETF not found");
        }
        
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (!isAdmin && !existingEtf.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        etf.setId(id);
        etf.setUserId(existingEtf.getUserId());
        return etfDao.save(etf);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteEtf(@PathVariable Long id, Principal principal) {
        Etf etf = etfDao.findById(id);
        if (etf == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ETF not found");
        }
        
        User user = userDao.getUserByUsername(principal.getName());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        if (!isAdmin && !etf.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        etfDao.deleteById(id);
    }
}
