package com.saptarshi.doubtconnect.entity.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.saptarshi.doubtconnect.security.AesEncryptor;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class PayoutDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonIgnore
    @Convert(converter = AesEncryptor.class)
    private String razorpayContactId;

    @JsonIgnore
    @Convert(converter = AesEncryptor.class)
    private String razorpayFundAccountId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "upi_id")
    private UpiDetails upiDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "bank_details_id")
    private BankDetails bankDetails;

    @Column(nullable = false)
    private String accountStatus = "INACTIVE";
}
