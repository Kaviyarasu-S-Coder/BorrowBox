import { api } from './api';
import { ApiResponse, Notification, PageResponse } from '../types';

export const notificationService = {
  async getNotifications(page = 0, size = 15): Promise<PageResponse<Notification>> {
    const res = await api.get<ApiResponse<PageResponse<Notification>>>('/notifications', {
      params: { page, size },
    });
    return res.data.data;
  },

  async getUnreadCount(): Promise<number> {
    const res = await api.get<ApiResponse<{ unreadCount: number }>>('/notifications/unread-count');
    return res.data.data.unreadCount;
  },

  async markAsRead(notificationId: number): Promise<void> {
    await api.put(`/notifications/${notificationId}/read`);
  },

  async markAllAsRead(): Promise<void> {
    await api.put('/notifications/read-all');
  },
};
