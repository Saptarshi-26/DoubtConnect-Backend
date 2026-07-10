package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.*;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.service.AuthService;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.hibernate.annotations.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> saveUser(@RequestBody SignUpRequest sign){
        return new ResponseEntity<>(authService.signUp(sign), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest){
        System.out.println(loginRequest.getUsername());
        System.out.println(loginRequest.getPassword());

        AuthResponse response = authService.login(loginRequest);

        if (response == null) {
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(
            @RequestBody GoogleLoginRequest request) {

        try {

            AuthResponse response =
                    authService.googleLogin(request.getGoogleIdToken());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {

            if ("USER_NOT_FOUND".equals(e.getMessage())) {

                return new ResponseEntity<>(
                        new AuthMessageResponse("USER_NOT_FOUND"),
                        HttpStatus.NOT_FOUND
                );

            }

            return new ResponseEntity<>(
                    new AuthMessageResponse("Google login failed"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

}
