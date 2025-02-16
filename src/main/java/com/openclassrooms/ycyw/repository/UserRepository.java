package com.openclassrooms.ycyw.repository;


import com.openclassrooms.ycyw.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User repository
 * @author Wilhelm Zwertvaegher
 * Date:02/16/2025
 * Time:15:58
 */

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
