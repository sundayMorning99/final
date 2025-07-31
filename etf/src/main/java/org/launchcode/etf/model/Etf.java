package org.launchcode.etf.model;

import java.math.BigDecimal;

public class Etf {
    private Long id;
    private String ticker;
    private String description;
    private String assetClass;
    private BigDecimal expenseRatio;
    private Long userId;
    private Boolean isPublic;

    public Etf() {}
    
    public Etf(Long id, String ticker, String description, String assetClass, 
               BigDecimal expenseRatio, Long userId, Boolean isPublic) {
        this.id = id;
        this.ticker = ticker;
        this.description = description;
        this.assetClass = assetClass;
        this.expenseRatio = expenseRatio;
        this.userId = userId;
        this.isPublic = isPublic;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAssetClass() { return assetClass; }
    public void setAssetClass(String assetClass) { this.assetClass = assetClass; }
    
    public BigDecimal getExpenseRatio() { return expenseRatio; }
    public void setExpenseRatio(BigDecimal expenseRatio) { this.expenseRatio = expenseRatio; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
}
