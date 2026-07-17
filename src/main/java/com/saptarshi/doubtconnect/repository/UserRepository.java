package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User  , Long > {
    public Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

}
