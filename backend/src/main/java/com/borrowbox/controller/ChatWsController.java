package com.borrowbox.controller;

import com.borrowbox.dto.request.SendMessageDto;
import com.borrowbox.dto.response.ChatMessageResponse;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void handleIncomingSocketMessage(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Payload SendMessageDto dto
    ) {
        log.info("Received WebSocket chat message payload for conv ID={}", dto.getConversationId());
        if (currentUser != null) {
            chatService.sendMessage(currentUser, dto);
        }
    }
}
