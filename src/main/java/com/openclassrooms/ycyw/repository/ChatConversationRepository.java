package com.openclassrooms.ycyw.repository;


import com.openclassrooms.ycyw.model.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * User repository
 * @author Wilhelm Zwertvaegher
 * Date:02/16/2025
 * Time:15:58
 */

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Integer> {
    Optional<ChatConversation> findById(UUID id);
}
