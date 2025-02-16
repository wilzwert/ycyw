package com.openclassrooms.ycyw.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name="chat_conversations")
@EntityListeners({AuditingEntityListener.class})
public class ChatConversation {
   @Id
   @Generated
   @UuidGenerator
   private UUID id;

   @CreatedDate
   @Column(name = "created_at")
   private LocalDateTime createdAt;

   @Column(name = "ended_at")
   private LocalDateTime endedAt;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "initiator_user_id", nullable = true)
   // let db handle cascade deletion
   @OnDelete(action = OnDeleteAction.SET_NULL)
   private User initiator;

   @Column(name="initiator_username")
   private String initiatorUsername;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "handler_user_id", nullable = true)
   // let db handle cascade deletion
   @OnDelete(action = OnDeleteAction.RESTRICT)
   private User handler;

@OneToMany(fetch = FetchType.LAZY, mappedBy = "conversation")
   private List<ChatMessage> messages;
}
