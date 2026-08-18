import { api } from './api';
import { AdminStats, AdminUser, ApiResponse, PageResponse } from '../types';

export const adminService = {
  async getDashboardStats(): Promise<AdminStats> {
    const res = await api.get<ApiResponse<AdminStats>>('/admin/dashboard/stats');
    return res.data.data;
  },

  async getPlatformStats(): Promise<AdminStats> {
    return this.getDashboardStats();
  },

  async getUsers(page = 0, size = 50, search?: string, isActive?: boolean): Promise<PageResponse<AdminUser>> {
    const res = await api.get<ApiResponse<PageResponse<AdminUser>>>('/admin/users', {
      params: { search, isActive, page, size },
    });
    return res.data.data;
  },

  async getAllUsers(search?: string, isActive?: boolean, page = 0, size = 20): Promise<PageResponse<AdminUser>> {
    return this.getUsers(page, size, search, isActive);
  },

  async toggleUserStatus(userId: number): Promise<AdminUser> {
    const res = await api.put<ApiResponse<AdminUser>>(`/admin/users/${userId}/toggle-status`);
    return res.data.data;
  },

  async toggleUserActiveStatus(userId: number): Promise<AdminUser> {
    return this.toggleUserStatus(userId);
  },

  async toggleUserVerify(userId: number): Promise<AdminUser> {
    const res = await api.put<ApiResponse<AdminUser>>(`/admin/users/${userId}/toggle-verify`);
    return res.data.data;
  },

  async toggleUserVerification(userId: number): Promise<AdminUser> {
    return this.toggleUserVerify(userId);
  },

  // Job triggers
  async triggerOverdueJob(): Promise<{ message: string; processedCount: number }> {
    const res = await api.post<ApiResponse<{ processedCount: number }>>('/admin/jobs/trigger-overdue');
    return {
      message: res.data.message || `Processed ${res.data.data?.processedCount || 0} overdue items`,
      processedCount: res.data.data?.processedCount || 0,
    };
  },

  async triggerRemindersJob(): Promise<{ message: string; dispatchedCount: number }> {
    const res = await api.post<ApiResponse<{ dispatchedCount: number }>>('/admin/jobs/trigger-reminders');
    return {
      message: res.data.message || `Dispatched ${res.data.data?.dispatchedCount || 0} reminders`,
      dispatchedCount: res.data.data?.dispatchedCount || 0,
    };
  },

  async triggerExpiredRequestsJob(): Promise<{ message: string; expiredCount: number }> {
    const res = await api.post<ApiResponse<{ expiredCount: number }>>('/admin/jobs/trigger-expired-requests');
    return {
      message: res.data.message || `Expired ${res.data.data?.expiredCount || 0} stale requests`,
      expiredCount: res.data.data?.expiredCount || 0,
    };
  },
};
