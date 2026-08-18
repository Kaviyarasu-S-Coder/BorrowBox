package com.borrowbox.service.impl;

import com.borrowbox.dto.request.SendMessageDto;
import com.borrowbox.dto.response.ChatMessageResponse;
import com.borrowbox.dto.response.ConversationResponse;
import com.borrowbox.entity.*;
import com.borrowbox.exception.BadRequestException;
import com.borrowbox.exception.ForbiddenException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.exception.UnauthorizedException;
import com.borrowbox.repository.*;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.ChatService;
import com.borrowbox.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final BorrowTransactionRepository transactionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(UserPrincipal currentUser, SendMessageDto dto) {
        if (currentUser == null) {
            throw new UnauthorizedException("User not authenticated.");
        }

        User sender = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Conversation conversation;

        if (dto.getConversationId() != null) {
            conversation = conversationRepository.findById(dto.getConversationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", dto.getConversationId()));

            validateParticipant(currentUser.getId(), conversation);
        } else if (dto.getRecipientId() != null) {
            if (dto.getRecipientId().equals(currentUser.getId())) {
                throw new BadRequestException("You cannot send a message to yourself.");
            }
            conversation = getOrCreateConversationEntity(sender, dto.getRecipientId(), dto.getBorrowRequestId(), dto.getTransactionId());
        } else {
            throw new BadRequestException("Either conversationId or recipientId must be provided.");
        }

        User recipient = conversation.getParticipant1().getId().equals(sender.getId())
                ? conversation.getParticipant2()
                : conversation.getParticipant1();

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .recipient(recipient)
                .content(dto.getContent().trim())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);

        // Update conversation summary
        conversation.setLastMessage(saved.getContent());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatMessageResponse response = mapToMessageResponse(saved);

        // 1. Broadcast via WebSocket to recipient topic & conversation topic
        try {
            messagingTemplate.convertAndSend("/topic/conversations/" + conversation.getId(), response);
            messagingTemplate.convertAndSend("/topic/messages/" + recipient.getId(), response);
        } catch (Exception e) {
            log.warn("Failed to broadcast chat message over WebSocket: {}", e.getMessage());
        }

        // 2. Dispatch in-app notification
        notificationService.createNotification(
                recipient,
                NotificationType.CHAT_MESSAGE,
                "New message from " + sender.getFullName(),
                saved.getContent().length() > 60 ? saved.getContent().substring(0, 57) + "..." : saved.getContent(),
                "/chat?conversationId=" + conversation.getId(),
                conversation.getId()
        );

        log.info("Sent chat message ID={} in conversation ID={} from user ID={} to user ID={}",
                saved.getId(), conversation.getId(), sender.getId(), recipient.getId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getUserConversations(UserPrincipal currentUser, Pageable pageable) {
        if (currentUser == null) {
            throw new UnauthorizedException("User not authenticated.");
        }

        return conversationRepository.findUserConversations(currentUser.getId(), pageable)
                .map(c -> mapToConversationResponse(c, currentUser.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getConversationMessages(UserPrincipal currentUser, Long conversationId, Pageable pageable) {
        if (currentUser == null) {
            throw new UnauthorizedException("User not authenticated.");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        validateParticipant(currentUser.getId(), conversation);

        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(this::mapToMessageResponse);
    }

    @Override
    @Transactional
    public ConversationResponse getOrCreateConversation(UserPrincipal currentUser, Long recipientId, Long borrowRequestId, Long transactionId) {
        if (currentUser == null) {
            throw new UnauthorizedException("User not authenticated.");
        }

        User sender = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Conversation conversation = getOrCreateConversationEntity(sender, recipientId, borrowRequestId, transactionId);
        return mapToConversationResponse(conversation, currentUser.getId());
    }

    @Override
    @Transactional
    public void markConversationAsRead(UserPrincipal currentUser, Long conversationId) {
        if (currentUser == null) {
            throw new UnauthorizedException("User not authenticated.");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        validateParticipant(currentUser.getId(), conversation);

        messageRepository.markConversationMessagesAsRead(conversationId, currentUser.getId(), LocalDateTime.now());
        log.info("Marked messages in conversation ID={} as read for user ID={}", conversationId, currentUser.getId());
    }

    private Conversation getOrCreateConversationEntity(User sender, Long recipientId, Long borrowRequestId, Long transactionId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recipientId));

        return conversationRepository.findExistingConversation(sender.getId(), recipient.getId(), borrowRequestId)
                .orElseGet(() -> {
                    BorrowRequest req = null;
                    if (borrowRequestId != null) {
                        req = borrowRequestRepository.findById(borrowRequestId).orElse(null);
                    }

                    BorrowTransaction tx = null;
                    if (transactionId != null) {
                        tx = transactionRepository.findById(transactionId).orElse(null);
                    }

                    Conversation newConv = Conversation.builder()
                            .participant1(sender)
                            .participant2(recipient)
                            .borrowRequest(req)
                            .transaction(tx)
                            .build();

                    return conversationRepository.save(newConv);
                });
    }

    private void validateParticipant(Long userId, Conversation conv) {
        boolean isP1 = conv.getParticipant1().getId().equals(userId);
        boolean isP2 = conv.getParticipant2().getId().equals(userId);

        if (!isP1 && !isP2) {
            throw new ForbiddenException("You are not a participant in this conversation.");
        }
    }

    private ChatMessageResponse mapToMessageResponse(Message m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFullName())
                .senderProfileImage(m.getSender().getProfileImageUrl())
                .recipientId(m.getRecipient().getId())
                .recipientName(m.getRecipient().getFullName())
                .content(m.getContent())
                .isRead(m.isRead())
                .readAt(m.getReadAt())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private ConversationResponse mapToConversationResponse(Conversation c, Long currentUserId) {
        User otherUser = c.getParticipant1().getId().equals(currentUserId)
                ? c.getParticipant2()
                : c.getParticipant1();

        Long itemId = null;
        String itemTitle = null;
        String itemImage = null;

        if (c.getTransaction() != null && c.getTransaction().getItem() != null) {
            Item it = c.getTransaction().getItem();
            itemId = it.getId();
            itemTitle = it.getTitle();
            if (it.getImages() != null && !it.getImages().isEmpty()) {
                itemImage = it.getImages().get(0).getImageUrl();
            }
        } else if (c.getBorrowRequest() != null && c.getBorrowRequest().getItem() != null) {
            Item it = c.getBorrowRequest().getItem();
            itemId = it.getId();
            itemTitle = it.getTitle();
            if (it.getImages() != null && !it.getImages().isEmpty()) {
                itemImage = it.getImages().get(0).getImageUrl();
            }
        }

        return ConversationResponse.builder()
                .id(c.getId())
                .otherUserId(otherUser.getId())
                .otherUserName(otherUser.getFullName())
                .otherUserProfileImage(otherUser.getProfileImageUrl())
                .otherUserRating(otherUser.getAverageRating())
                .borrowRequestId(c.getBorrowRequest() != null ? c.getBorrowRequest().getId() : null)
                .transactionId(c.getTransaction() != null ? c.getTransaction().getId() : null)
                .itemId(itemId)
                .itemTitle(itemTitle)
                .itemPrimaryImage(itemImage)
                .lastMessage(c.getLastMessage())
                .lastMessageAt(c.getLastMessageAt())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
