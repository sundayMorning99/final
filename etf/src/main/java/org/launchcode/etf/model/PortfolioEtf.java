package org.launchcode.etf.model;

public class PortfolioEtf {
    private Long id;
    private Long portfolioId;
    private Long etfId;
    
    public PortfolioEtf() {}
    
    public PortfolioEtf(Long id, Long portfolioId, Long etfId) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.etfId = etfId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }
    
    public Long getEtfId() { return etfId; }
    public void setEtfId(Long etfId) { this.etfId = etfId; }
}

