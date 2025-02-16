package com.openclassrooms.ycyw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * @author Wilhelm Zwertvaegher
 * Date:05/02/2025
 * Time:15:59
 */

@Data
@Accessors(chain = true)
@Entity
@Table(name="chat_message")
@EntityListeners({AuditingEntityListener.class})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column
    private String sender;

    @Column
    private String recipient;

    @Column
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_conversation_id", nullable = false)
    private ChatConversation conversation;
}
