package com.saptarshi.doubtconnect.payment.dto;

import lombok.Data;

@Data
public class PayOutDetailsDto {

    private String upiId;


    private String accountNumber;

    private String ifscCode;


    private String accountHolderName;

//   private String razorpayContactId;
//
//    private String razorpayFundAccountId;

}
