package org.launchcode.etf.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaticController {
    
    @GetMapping(value = {"/", "/etfs", "/portfolios", "/etfs/**", "/portfolios/**"}, 
                produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> index() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ClassPathResource("static/index.html"));
    }
}
