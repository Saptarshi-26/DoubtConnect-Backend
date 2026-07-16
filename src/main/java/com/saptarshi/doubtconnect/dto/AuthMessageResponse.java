package com.saptarshi.doubtconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AuthMessageResponse {

    // getters and setters
    private String message;
    private String token;
    private String role;
    private String username;
    private Long profileId;

    // existing constructor — used for error/plain messages
    public AuthMessageResponse(String message) {
        this.message = message;
    }

    // new constructor — used when signup succeeds and we auto-login
    public AuthMessageResponse(String message, String token, String role, String username, Long profileId) {
        this.message = message;
        this.token = token;
        this.role = role;
        this.username = username;
        this.profileId = profileId;
    }

}