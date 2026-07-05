package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.ForgotPasswordRequest;
import com.saptarshi.doubtconnect.dto.ResetPasswordRequest;
import com.saptarshi.doubtconnect.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reset-password")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        boolean success =
                passwordResetService.forgotPassword(request);

        return success
                ? ResponseEntity.ok("Reset email sent.")
                : ResponseEntity.badRequest()
                .body("Email not found.");
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        boolean success =
                passwordResetService.resetPassword(request);

        return success
                ? ResponseEntity.ok("Password updated.")
                : ResponseEntity.badRequest()
                .body("Invalid or expired token.");
    }
}
