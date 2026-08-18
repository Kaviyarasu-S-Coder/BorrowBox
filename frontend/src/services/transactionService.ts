import { api } from './api';
import {
  ApiResponse,
  BorrowTransaction,
  ConditionStage,
  PageResponse,
  TransactionCondition,
} from '../types';

export interface VerifyPickupPayload {
  pickupCode: string;
  notes?: string;
}

export interface VerifyReturnPayload {
  returnCode: string;
  notes?: string;
}

export interface LogConditionPayload {
  stage: ConditionStage;
  notes: string;
  imageUrls: string[];
}

export const transactionService = {
  async getTransactionById(id: number): Promise<BorrowTransaction> {
    const res = await api.get<ApiResponse<BorrowTransaction>>(`/transactions/${id}`);
    return res.data.data;
  },

  async getMyBorrowTransactions(page = 0, size = 10): Promise<PageResponse<BorrowTransaction>> {
    const res = await api.get<ApiResponse<PageResponse<BorrowTransaction>>>('/transactions/my-borrowed', {
      params: { page, size },
    });
    return res.data.data;
  },

  async getMyLendTransactions(page = 0, size = 10): Promise<PageResponse<BorrowTransaction>> {
    const res = await api.get<ApiResponse<PageResponse<BorrowTransaction>>>('/transactions/my-lent', {
      params: { page, size },
    });
    return res.data.data;
  },

  async verifyPickup(txId: number, payload: VerifyPickupPayload): Promise<BorrowTransaction> {
    const res = await api.put<ApiResponse<BorrowTransaction>>(`/transactions/${txId}/verify-pickup`, payload);
    return res.data.data;
  },

  async verifyReturn(txId: number, payload: VerifyReturnPayload): Promise<BorrowTransaction> {
    const res = await api.put<ApiResponse<BorrowTransaction>>(`/transactions/${txId}/verify-return`, payload);
    return res.data.data;
  },

  async logCondition(txId: number, payload: LogConditionPayload): Promise<TransactionCondition> {
    const res = await api.post<ApiResponse<TransactionCondition>>(`/transactions/${txId}/conditions`, payload);
    return res.data.data;
  },

  async getConditions(txId: number): Promise<TransactionCondition[]> {
    const res = await api.get<ApiResponse<TransactionCondition[]>>(`/transactions/${txId}/conditions`);
    return res.data.data;
  },
};
