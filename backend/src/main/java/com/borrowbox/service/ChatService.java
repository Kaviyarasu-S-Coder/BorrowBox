package com.borrowbox.service;

import com.borrowbox.dto.request.SendMessageDto;
import com.borrowbox.dto.response.ChatMessageResponse;
import com.borrowbox.dto.response.ConversationResponse;
import com.borrowbox.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    ChatMessageResponse sendMessage(UserPrincipal currentUser, SendMessageDto dto);

    Page<ConversationResponse> getUserConversations(UserPrincipal currentUser, Pageable pageable);

    Page<ChatMessageResponse> getConversationMessages(UserPrincipal currentUser, Long conversationId, Pageable pageable);

    ConversationResponse getOrCreateConversation(UserPrincipal currentUser, Long recipientId, Long borrowRequestId, Long transactionId);

    void markConversationAsRead(UserPrincipal currentUser, Long conversationId);
}
