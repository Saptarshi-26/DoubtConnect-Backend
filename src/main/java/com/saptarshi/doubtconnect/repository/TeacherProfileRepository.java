package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile,Long> {
    @EntityGraph(attributePaths = {
            "user",
            "payoutDetails",
            "subjects"
    })
    @Override
    List<TeacherProfile> findAll();

    @Query("""
SELECT t
FROM TeacherProfile t
WHERE t.active = true
AND t.payoutDetails.accountStatus = 'ACTIVE'
""")
    @EntityGraph(attributePaths = {
            "user",
            "payoutDetails",
            "subjects"
    })
    List<TeacherProfile> findAllActiveTeachers();


    @EntityGraph(attributePaths = {
            "user",
            "payoutDetails",
            "payoutDetails.upiDetails",
            "payoutDetails.bankDetails",
            "subjects"
    })
    Optional<TeacherProfile> findByUser(User user);
    @EntityGraph(attributePaths = {
            "user",
            "payoutDetails",
            "payoutDetails.upiDetails",
            "payoutDetails.bankDetails",
            "subjects"
    })
    Optional<TeacherProfile> findByGoogleEmail(String googleEmail);
    @EntityGraph(attributePaths = {
            "user",
            "payoutDetails",
            "payoutDetails.upiDetails",
            "payoutDetails.bankDetails",
            "subjects"
    })
    Optional<TeacherProfile> findByUserUsername(String username);

}
