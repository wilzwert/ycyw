package com.openclassrooms.ycyw.repository;

import com.openclassrooms.ycyw.model.ChatMessage;
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
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    Optional<ChatMessage> findById(Long id);
}
