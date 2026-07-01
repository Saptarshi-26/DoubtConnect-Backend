package com.saptarshi.doubtconnect.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String role;
    private String username;
    private Long profileId;

    public AuthResponse() {}

    public AuthResponse(String token, String role, String username, Long profileId) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.profileId = profileId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
}
