import { api } from './api';
import { ApiResponse, ItemSummary, PageResponse } from '../types';

export interface FavoriteStatus {
  isFavorited: boolean;
  totalFavorites: number;
}

export const favoriteService = {
  async toggleFavorite(itemId: number): Promise<FavoriteStatus> {
    const res = await api.post<ApiResponse<FavoriteStatus>>(`/favorites/${itemId}/toggle`);
    return res.data.data;
  },

  async getMyFavorites(page = 0, size = 12): Promise<PageResponse<ItemSummary>> {
    const res = await api.get<ApiResponse<PageResponse<ItemSummary>>>('/favorites', {
      params: { page, size },
    });
    return res.data.data;
  },

  async getFavoriteStatus(itemId: number): Promise<FavoriteStatus> {
    const res = await api.get<ApiResponse<FavoriteStatus>>(`/favorites/${itemId}/status`);
    return res.data.data;
  },
};
