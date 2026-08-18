package com.borrowbox.controller;

import com.borrowbox.dto.request.SendMessageDto;
import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.dto.response.ChatMessageResponse;
import com.borrowbox.dto.response.ConversationResponse;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Real-Time Chat", description = "Endpoints for user conversations, messaging, and chat history")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/messages")
    @Operation(summary = "Send chat message", description = "Sends a message in a conversation and triggers real-time WebSocket broadcast.")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SendMessageDto dto
    ) {
        ChatMessageResponse message = chatService.sendMessage(currentUser, dto);
        return new ResponseEntity<>(ApiResponse.success(message, "Message sent successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get user conversations", description = "Returns active conversation threads for the current user.")
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> getConversations(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "lastMessageAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ConversationResponse> conversations = chatService.getUserConversations(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Get conversation messages", description = "Returns paginated chat history for a specific conversation.")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long conversationId,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ChatMessageResponse> messages = chatService.getConversationMessages(currentUser, conversationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/conversations/start")
    @Operation(summary = "Start or fetch conversation", description = "Finds or starts a conversation thread with another user.")
    public ResponseEntity<ApiResponse<ConversationResponse>> startConversation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam Long recipientId,
            @RequestParam(required = false) Long borrowRequestId,
            @RequestParam(required = false) Long transactionId
    ) {
        ConversationResponse conv = chatService.getOrCreateConversation(currentUser, recipientId, borrowRequestId, transactionId);
        return ResponseEntity.ok(ApiResponse.success(conv));
    }

    @PutMapping("/conversations/{conversationId}/read")
    @Operation(summary = "Mark conversation as read", description = "Marks all unread incoming messages in a conversation as read.")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long conversationId
    ) {
        chatService.markConversationAsRead(currentUser, conversationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Conversation marked as read"));
    }
}
