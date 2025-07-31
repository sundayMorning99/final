package org.launchcode.etf.dto;

public class UpdateUserRequest {
    private String username;
    private String role;
    private String newPassword; 

    public UpdateUserRequest() {}
    
    public UpdateUserRequest(String username, String role, String newPassword) {
        this.username = username;
        this.role = role;
        this.newPassword = newPassword;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
