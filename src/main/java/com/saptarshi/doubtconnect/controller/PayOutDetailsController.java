package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.payment.dto.PayOutDetailsDto;
import com.saptarshi.doubtconnect.payment.service.PayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payout")
public class PayOutDetailsController {

    @Autowired
    private PayoutService payoutService;

    @PostMapping
    public ResponseEntity<?> savePayoutDetails(
            @RequestBody PayOutDetailsDto dto,
            Authentication authentication) {

        String response = payoutService.savePayoutDetails(
                dto,
                authentication.getName()
        );

        return response.equals("Account details added successfully ")
                ? new ResponseEntity<>(response, HttpStatus.CREATED)
                : new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @PutMapping
    public ResponseEntity<?> updatePayoutDetails(
            @RequestBody PayOutDetailsDto dto,
            Authentication authentication) {

        String response = payoutService.updatePaymentDetails(
                dto,
                authentication.getName()
        );

        return response.equals("Update successful ")
                ? new ResponseEntity<>(response, HttpStatus.OK)
                : new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
