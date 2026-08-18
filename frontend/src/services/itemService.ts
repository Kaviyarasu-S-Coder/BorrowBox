import { api } from './api';
import {
  ApiResponse,
  ItemCondition,
  ItemDetail,
  ItemStatus,
  ItemSummary,
  LendingMode,
  PageResponse,
} from '../types';

export interface ItemFilterParams {
  query?: string;
  categorySlug?: string;
  subCategory?: string;
  condition?: ItemCondition;
  minDailyRate?: number;
  maxDailyRate?: number;
  lendingMode?: LendingMode;
  location?: string;
  startDate?: string;
  endDate?: string;
  status?: ItemStatus;
  page?: number;
  size?: number;
  sort?: string;
}

export interface CreateItemPayload {
  categoryId: number;
  subCategory?: string;
  title: string;
  description: string;
  condition: ItemCondition;
  estimatedValue?: number;
  depositAmount?: number;
  dailyRate?: number;
  lendingMode?: LendingMode;
  location?: string;
  latitude?: number;
  longitude?: number;
  minBorrowDays?: number;
  maxBorrowDays?: number;
  borrowingRules?: string;
  imageUrls?: string[];
}

export interface UpdateItemPayload extends Partial<CreateItemPayload> {
  status?: ItemStatus;
}

export const itemService = {
  async searchItems(params: ItemFilterParams = {}): Promise<PageResponse<ItemSummary>> {
    const res = await api.get<ApiResponse<PageResponse<ItemSummary>>>('/items', { params });
    return res.data.data;
  },

  async getItemById(id: number): Promise<ItemDetail> {
    const res = await api.get<ApiResponse<ItemDetail>>(`/items/${id}`);
    return res.data.data;
  },

  async getRecentlyListed(): Promise<ItemSummary[]> {
    const res = await api.get<ApiResponse<ItemSummary[]>>('/items/featured/recent');
    return res.data.data;
  },

  async getPopularItems(): Promise<ItemSummary[]> {
    const res = await api.get<ApiResponse<ItemSummary[]>>('/items/featured/popular');
    return res.data.data;
  },

  async getMyItems(page = 0, size = 10): Promise<PageResponse<ItemSummary>> {
    const res = await api.get<ApiResponse<PageResponse<ItemSummary>>>('/items/my', {
      params: { page, size },
    });
    return res.data.data;
  },

  async createItem(payload: CreateItemPayload): Promise<ItemDetail> {
    const res = await api.post<ApiResponse<ItemDetail>>('/items', payload);
    return res.data.data;
  },

  async updateItem(id: number, payload: UpdateItemPayload): Promise<ItemDetail> {
    const res = await api.put<ApiResponse<ItemDetail>>(`/items/${id}`, payload);
    return res.data.data;
  },

  async deleteItem(id: number): Promise<void> {
    await api.delete(`/items/${id}`);
  },

  async uploadItemImage(itemId: number, file: File, isPrimary = false): Promise<ItemDetail> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('isPrimary', String(isPrimary));

    const res = await api.post<ApiResponse<ItemDetail>>(`/items/${itemId}/images`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data.data;
  },
};
