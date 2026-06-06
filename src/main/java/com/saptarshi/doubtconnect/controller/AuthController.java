package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.LoginRequest;
import com.saptarshi.doubtconnect.dto.SignUpRequest;
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

        return new ResponseEntity<>(
                authService.login(loginRequest),
                HttpStatus.OK);
    }

}
