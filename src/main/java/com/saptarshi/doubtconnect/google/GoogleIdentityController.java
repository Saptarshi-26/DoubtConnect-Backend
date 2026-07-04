package com.saptarshi.doubtconnect.google;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/google")
public class GoogleIdentityController {

    @Autowired
    private GoogleIdentityService googleIdentityService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyGoogle(
            @RequestBody GoogleVerificationRequest request) {

        try {

            String email = googleIdentityService
                    .verifyAndGetEmail(request.getGoogleIdToken());

            if (email == null) {
                return new ResponseEntity<>(
                        "Invalid Google verification",
                        HttpStatus.BAD_REQUEST
                );
            }

            return new ResponseEntity<>(
                    new GoogleVerificationResponse(email),
                    HttpStatus.OK
            );

        } catch (Exception e) {

            return new ResponseEntity<>(
                    "Invalid Google verification",
                    HttpStatus.BAD_REQUEST
            );

        }

    }
}
