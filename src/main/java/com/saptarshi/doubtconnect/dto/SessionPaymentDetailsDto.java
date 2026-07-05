package com.saptarshi.doubtconnect.dto;

import lombok.Data;

@Data
public class SessionPaymentDetailsDto {
    private String upiId;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
}
