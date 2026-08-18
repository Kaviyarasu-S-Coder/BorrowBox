import { api } from './api';
import { ApiResponse, PageResponse, Dispute, Report } from '../types';

export interface CreateDisputePayload {
  transactionId: number;
  reason: string;
  description: string;
  evidenceImages?: string[];
}

export interface ResolveDisputePayload {
  status?: string;
  adminDecision?: string;
  resolutionNotes?: string;
  refundEscrow?: boolean;
  penalizeUser?: boolean;
}

export interface CreateReportPayload {
  reportedUserId?: number;
  reportedItemId?: number;
  reason: string;
  description: string;
}

export interface ResolveReportPayload {
  action?: string;
  status?: string;
  adminNotes?: string;
  deactivateItem?: boolean;
  banUser?: boolean;
}

export const disputeService = {
  async createDispute(payload: CreateDisputePayload): Promise<Dispute> {
    const res = await api.post<ApiResponse<Dispute>>('/disputes', payload);
    return res.data.data;
  },

  async getDisputeById(id: number): Promise<Dispute> {
    const res = await api.get<ApiResponse<Dispute>>(`/disputes/${id}`);
    return res.data.data;
  },

  async getMyDisputes(page = 0, size = 10): Promise<PageResponse<Dispute>> {
    const res = await api.get<ApiResponse<PageResponse<Dispute>>>('/disputes/my', {
      params: { page, size },
    });
    return res.data.data;
  },

  async getAllDisputes(status?: string, page = 0, size = 15): Promise<PageResponse<Dispute>> {
    const res = await api.get<ApiResponse<PageResponse<Dispute>>>('/disputes/admin', {
      params: { status, page, size },
    });
    return res.data.data;
  },

  async getAdminDisputes(page = 0, size = 15, status?: string): Promise<PageResponse<Dispute>> {
    return this.getAllDisputes(status, page, size);
  },

  async resolveDispute(id: number, payload: ResolveDisputePayload): Promise<Dispute> {
    const body = {
      status: payload.status || (payload.refundEscrow ? 'RESOLVED_FAVOR_BORROWER' : 'RESOLVED_FAVOR_OWNER'),
      adminDecision: payload.adminDecision || payload.resolutionNotes || 'Dispute resolved by administrator review.',
      resolutionNotes: payload.resolutionNotes || payload.adminDecision,
    };
    const res = await api.put<ApiResponse<Dispute>>(`/disputes/admin/${id}/resolve`, body);
    return res.data.data;
  },

  // Reports
  async createReport(payload: CreateReportPayload): Promise<Report> {
    const res = await api.post<ApiResponse<Report>>('/reports', payload);
    return res.data.data;
  },

  async getAllReports(status?: string, page = 0, size = 15): Promise<PageResponse<Report>> {
    const res = await api.get<ApiResponse<PageResponse<Report>>>('/reports/admin', {
      params: { status, page, size },
    });
    return res.data.data;
  },

  async getAdminReports(page = 0, size = 15, status?: string): Promise<PageResponse<Report>> {
    return this.getAllReports(status, page, size);
  },

  async resolveReport(id: number, payload: ResolveReportPayload): Promise<Report> {
    const body = {
      status: payload.status || (payload.action === 'DISMISSED' ? 'DISMISSED' : 'RESOLVED'),
      adminNotes: payload.adminNotes || `Action taken: ${payload.action}`,
      deactivateItem: payload.deactivateItem || payload.action === 'ITEM_DEACTIVATED',
      banUser: payload.banUser || payload.action === 'USER_BANNED',
    };
    const res = await api.put<ApiResponse<Report>>(`/reports/admin/${id}/resolve`, body);
    return res.data.data;
  },
};
