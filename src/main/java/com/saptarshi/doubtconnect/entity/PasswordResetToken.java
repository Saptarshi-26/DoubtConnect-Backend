package com.saptarshi.doubtconnect.entity;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiry;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
