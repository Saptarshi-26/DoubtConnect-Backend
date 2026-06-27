package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.google.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth/google")
public class GoogleOAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @GetMapping("/connect")
    public ResponseEntity<Void> connectGoogle(  @RequestParam Long teacherProfileId) {

        return ResponseEntity
                .status(302)
                .header("Location", googleOAuthService.getAuthorizationUrl(teacherProfileId))
                .build();
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code ,
                           @RequestParam("state") Long teacherProfileId){
        return googleOAuthService.exchangeCodeForAccessToken(code,teacherProfileId);    }
}
