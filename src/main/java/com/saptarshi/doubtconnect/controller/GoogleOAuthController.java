package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.google.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/google")
public class GoogleOAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @GetMapping("/connect")
    public ResponseEntity<Void> connectGoogle() {

        return ResponseEntity
                .status(302)
                .header("Location", googleOAuthService.getAuthorizationUrl())
                .build();
    }
}
