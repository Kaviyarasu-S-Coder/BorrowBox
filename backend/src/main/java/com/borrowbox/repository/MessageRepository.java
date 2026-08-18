package com.borrowbox.repository;

import com.borrowbox.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :now WHERE m.conversation.id = :convId AND m.recipient.id = :userId AND m.isRead = false")
    void markConversationMessagesAsRead(@Param("convId") Long convId, @Param("userId") Long userId, @Param("now") LocalDateTime now);
}
