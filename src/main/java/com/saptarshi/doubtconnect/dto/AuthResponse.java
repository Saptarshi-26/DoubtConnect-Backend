package com.saptarshi.doubtconnect.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

}
