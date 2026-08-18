import { api } from './api';
import { ApiResponse, UserProfile, UserSummary } from '../types';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  fullName: string;
  phone?: string;
  location?: string;
}

export interface AuthResponseData {
  accessToken: string;
  tokenType: string;
  refreshToken: string;
  user: UserSummary;
}

export interface UpdateProfilePayload {
  fullName?: string;
  bio?: string;
  phone?: string;
  location?: string;
  latitude?: number;
  longitude?: number;
}

export const authService = {
  async register(payload: RegisterPayload): Promise<AuthResponseData> {
    const res = await api.post<ApiResponse<AuthResponseData>>('/auth/register', payload);
    return res.data.data;
  },

  async login(payload: LoginPayload): Promise<AuthResponseData> {
    const res = await api.post<ApiResponse<AuthResponseData>>('/auth/login', payload);
    return res.data.data;
  },

  async getMe(): Promise<UserProfile> {
    const res = await api.get<ApiResponse<UserProfile>>('/users/me');
    return res.data.data;
  },

  async updateProfile(payload: UpdateProfilePayload): Promise<UserProfile> {
    const res = await api.put<ApiResponse<UserProfile>>('/users/me', payload);
    return res.data.data;
  },

  async getUserPublicProfile(userId: number): Promise<UserProfile> {
    const res = await api.get<ApiResponse<UserProfile>>(`/users/${userId}`);
    return res.data.data;
  },
};
