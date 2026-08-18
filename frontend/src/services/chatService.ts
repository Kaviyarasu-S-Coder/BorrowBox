import { api } from './api';
import { ApiResponse, ChatMessage, Conversation, PageResponse } from '../types';

export interface StartConversationPayload {
  recipientId: number;
  borrowRequestId?: number;
  transactionId?: number;
  initialMessage?: string;
}

export interface SendMessagePayload {
  conversationId: number;
  recipientId: number;
  content: string;
}

export const chatService = {
  async getConversations(page = 0, size = 20): Promise<PageResponse<Conversation>> {
    const res = await api.get<ApiResponse<PageResponse<Conversation>>>('/chat/conversations', {
      params: { page, size },
    });
    return res.data.data;
  },

  async startConversation(payload: StartConversationPayload): Promise<Conversation> {
    const res = await api.post<ApiResponse<Conversation>>('/chat/conversations', payload);
    return res.data.data;
  },

  async getMessages(conversationId: number, page = 0, size = 50): Promise<PageResponse<ChatMessage>> {
    const res = await api.get<ApiResponse<PageResponse<ChatMessage>>>(
      `/chat/conversations/${conversationId}/messages`,
      {
        params: { page, size },
      }
    );
    return res.data.data;
  },

  async sendMessage(payload: SendMessagePayload): Promise<ChatMessage> {
    const res = await api.post<ApiResponse<ChatMessage>>('/chat/messages', payload);
    return res.data.data;
  },

  async markAsRead(conversationId: number): Promise<void> {
    await api.put(`/chat/conversations/${conversationId}/read`);
  },
};
