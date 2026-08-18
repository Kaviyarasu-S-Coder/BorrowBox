import { api } from './api';
import { ApiResponse, PageResponse, Rating } from '../types';

export interface CreateRatingPayload {
  transactionId: number;
  score: number;
  communicationScore: number;
  punctualityScore: number;
  reliabilityScore: number;
  reviewComment?: string;
}

export const ratingService = {
  async submitRating(payload: CreateRatingPayload): Promise<Rating> {
    const res = await api.post<ApiResponse<Rating>>('/ratings', payload);
    return res.data.data;
  },

  async getUserRatings(userId: number, page = 0, size = 10): Promise<PageResponse<Rating>> {
    const res = await api.get<ApiResponse<PageResponse<Rating>>>(`/ratings/user/${userId}`, {
      params: { page, size },
    });
    return res.data.data;
  },
};
