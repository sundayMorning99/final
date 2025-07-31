package org.launchcode.etf.model;

public class Portfolio {
    private Long id;
    private String name;
    private Long userId;
    private Boolean isPublic;

    public Portfolio() {}
    
    public Portfolio(Long id, String name, Long userId, Boolean isPublic) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.isPublic = isPublic;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
}
