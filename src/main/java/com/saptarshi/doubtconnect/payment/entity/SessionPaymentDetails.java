package com.saptarshi.doubtconnect.payment.entity;

import com.saptarshi.doubtconnect.security.AesEncryptor;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import jakarta.persistence.Id;
@Entity
@Data
public class SessionPaymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Convert(converter = AesEncryptor.class)
    private String accountNumber;

    @Convert(converter = AesEncryptor.class)
    private String ifscCode;

    @Convert(converter = AesEncryptor.class)
    private String accountHolderName;

    @Convert(converter = AesEncryptor.class)
    private String upiId;
}


