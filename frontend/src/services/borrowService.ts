import { api } from './api';
import { ApiResponse, BorrowRequest, PageResponse } from '../types';

export interface CreateBorrowRequestPayload {
  itemId: number;
  startDate: string;
  endDate: string;
  purpose?: string;
  message: string;
}

export interface AvailabilityCheckResult {
  itemId: number;
  startDate: string;
  endDate: string;
  totalDays: number;
  isAvailable: boolean;
  message: string;
  depositRequired: number;
  dailyRate: number;
  estimatedRentalCost: number;
}

export interface DateRange {
  startDate: string;
  endDate: string;
  reason: string;
}

export const borrowService = {
  async checkAvailability(
    itemId: number,
    startDate: string,
    endDate: string
  ): Promise<AvailabilityCheckResult> {
    const res = await api.get<ApiResponse<AvailabilityCheckResult>>(
      `/items/${itemId}/availability`,
      {
        params: { startDate, endDate },
      }
    );
    return res.data.data;
  },

  async getBookedDateRanges(itemId: number, year?: number, month?: number): Promise<DateRange[]> {
    const res = await api.get<ApiResponse<DateRange[]>>(`/items/${itemId}/calendar`, {
      params: { year, month },
    });
    return res.data.data;
  },

  async createBorrowRequest(payload: CreateBorrowRequestPayload): Promise<BorrowRequest> {
    const res = await api.post<ApiResponse<BorrowRequest>>('/borrow-requests', payload);
    return res.data.data;
  },

  async getMyBorrowRequests(page = 0, size = 10): Promise<PageResponse<BorrowRequest>> {
    const res = await api.get<ApiResponse<PageResponse<BorrowRequest>>>('/borrow-requests/my-requests', {
      params: { page, size },
    });
    return res.data.data;
  },

  async getMyLendingRequests(page = 0, size = 10, status?: string): Promise<PageResponse<BorrowRequest>> {
    const res = await api.get<ApiResponse<PageResponse<BorrowRequest>>>('/borrow-requests/my-lending-requests', {
      params: { page, size, status },
    });
    return res.data.data;
  },

  async acceptRequest(requestId: number, responseMessage?: string): Promise<BorrowRequest> {
    const res = await api.put<ApiResponse<BorrowRequest>>(`/borrow-requests/${requestId}/accept`, {
      responseMessage,
    });
    return res.data.data;
  },

  async rejectRequest(requestId: number, reason: string): Promise<BorrowRequest> {
    const res = await api.put<ApiResponse<BorrowRequest>>(`/borrow-requests/${requestId}/reject`, {
      reason,
    });
    return res.data.data;
  },

  async cancelRequest(requestId: number, reason: string): Promise<BorrowRequest> {
    const res = await api.put<ApiResponse<BorrowRequest>>(`/borrow-requests/${requestId}/cancel`, {
      reason,
    });
    return res.data.data;
  },
};
