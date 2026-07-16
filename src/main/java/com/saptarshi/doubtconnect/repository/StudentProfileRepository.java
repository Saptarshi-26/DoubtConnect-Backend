package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile,Long> {
    @Override
    @EntityGraph(attributePaths = {
            "user",
            "favourites",
            "favourites.user",
            "favourites.payoutDetails",
            "favourites.payoutDetails.upiDetails",
            "favourites.payoutDetails.bankDetails"
    })
    Optional<StudentProfile> findById(Long id);
    @EntityGraph(attributePaths = {
            "user",
            "favourites",
            "favourites.user",
            "favourites.payoutDetails",
            "favourites.payoutDetails.upiDetails",
            "favourites.payoutDetails.bankDetails"
    })
    Optional<StudentProfile> findByUser(User user);
    @EntityGraph(attributePaths = {
            "user",
            "favourites",
            "favourites.user",
            "favourites.payoutDetails",
            "favourites.payoutDetails.upiDetails",
            "favourites.payoutDetails.bankDetails"
    })
    Optional<StudentProfile> findByUserUsername(String username);
    @EntityGraph(attributePaths = {
            "user",
            "favourites",
            "favourites.user",
            "favourites.payoutDetails",
            "favourites.payoutDetails.upiDetails",
            "favourites.payoutDetails.bankDetails"
    })
    Optional<StudentProfile> findByGoogleEmail(String googleEmail);
    @Modifying
    @Query(value = "DELETE FROM student_profiles_favourites WHERE favourites_id = :teacherId", nativeQuery = true)
    void removeFromAllFavourites(@Param("teacherId") Long teacherId);
}
