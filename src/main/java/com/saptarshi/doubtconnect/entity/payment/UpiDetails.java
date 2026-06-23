package com.saptarshi.doubtconnect.entity.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.saptarshi.doubtconnect.security.AesEncryptor;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class UpiDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonIgnore
    @Column(nullable = false)
    @Convert(converter = AesEncryptor.class)
    private String upiId;

}
