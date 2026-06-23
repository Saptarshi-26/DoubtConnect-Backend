package com.saptarshi.doubtconnect.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import lombok.Data;

@Data
public class PayOutDetailsDto {

    private String upiId;


    private String accountNumber;

    private String ifscCode;


    private String accountHolderName;

   private String razorpayContactId;

    private String razorpayFundAccountId;

}
