package com.saptarshi.doubtconnect.entity;


import com.saptarshi.doubtconnect.payment.entity.PayoutDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teacher_profiles")
public class TeacherProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ElementCollection
    private List<String > subjects= new ArrayList<>();

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String bio;

    @Column(nullable = false)
    private double ratePerThirtyMin;

    @Column
    private String profilePictureUrl;

    @Column(nullable = false)
    private int totalRating = 5;

    @Column(nullable = false)
    private int numberOfRatings = 1;

    @Column(nullable = false)
    private double rating=5;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="payout_details_id")
    private PayoutDetails payoutDetails;


}
