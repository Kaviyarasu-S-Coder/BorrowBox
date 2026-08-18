export type Role = 'ROLE_USER' | 'ROLE_ADMIN';

export type ItemCondition = 'NEW' | 'LIKE_NEW' | 'GOOD' | 'FAIR' | 'USED';

export type ItemStatus = 'AVAILABLE' | 'BORROWED' | 'RESERVED' | 'MAINTENANCE' | 'INACTIVE';

export type LendingMode = 'FREE' | 'DAILY_RATE' | 'DEPOSIT_ONLY' | 'RATE_AND_DEPOSIT';

export type RequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'EXPIRED';

export type TransactionStatus =
  | 'UPCOMING'
  | 'READY_FOR_PICKUP'
  | 'BORROWED'
  | 'RETURN_PENDING'
  | 'RETURNED'
  | 'COMPLETED'
  | 'OVERDUE'
  | 'DISPUTED'
  | 'CANCELLED';

export type ConditionStage = 'PRE_PICKUP' | 'POST_RETURN';

export type DisputeStatus = 'OPEN' | 'UNDER_REVIEW' | 'RESOLVED' | 'REJECTED' | 'CLOSED';

export type ReportStatus = 'OPEN' | 'PENDING' | 'UNDER_REVIEW' | 'RESOLVED' | 'DISMISSED';

export type NotificationType =
  | 'REQUEST_RECEIVED'
  | 'REQUEST_ACCEPTED'
  | 'REQUEST_REJECTED'
  | 'REQUEST_CANCELLED'
  | 'TRANSACTION_CREATED'
  | 'PICKUP_REMINDER'
  | 'PICKUP_CONFIRMED'
  | 'RETURN_REMINDER'
  | 'RETURN_PENDING'
  | 'RETURN_CONFIRMED'
  | 'OVERDUE'
  | 'DISPUTE_OPENED'
  | 'DISPUTE_UPDATED'
  | 'RATING_RECEIVED'
  | 'CHAT_MESSAGE'
  | 'ITEM_AVAILABLE';

export interface UserSummary {
  id: number;
  email: string;
  fullName: string;
  location?: string;
  profileImageUrl?: string;
  averageRating: number;
  ratingCount: number;
  reputationScore: number;
  roles: string[];
  role?: string;
  verified: boolean;
}

export interface UserProfile extends UserSummary {
  bio?: string;
  phone?: string;
  completedBorrowings: number;
  completedLendings: number;
  cancellationCount: number;
  disputeCount: number;
  createdAt: string;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  description?: string;
  icon: string;
  parentId?: number;
  active: boolean;
  subCategories?: Category[];
}

export interface ItemImage {
  id: number;
  imageUrl: string;
  isPrimary: boolean;
  displayOrder: number;
}

export interface ItemSummary {
  id: number;
  title: string;
  categoryName?: string;
  categorySlug?: string;
  subCategory?: string;
  condition: ItemCondition;
  estimatedValue?: number;
  depositAmount?: number;
  dailyRate?: number;
  lendingMode: LendingMode;
  location: string;
  status: ItemStatus;
  primaryImageUrl?: string;
  ownerId: number;
  ownerName: string;
  ownerRating: number;
  ownerReputation: number;
  borrowCount: number;
  createdAt: string;
}

export interface ItemDetail extends ItemSummary {
  description: string;
  categoryId?: number;
  latitude?: number;
  longitude?: number;
  minBorrowDays: number;
  maxBorrowDays: number;
  borrowingRules?: string;
  images: ItemImage[];
  ownerProfileImage?: string;
  ownerLocation?: string;
  ownerRatingCount: number;
  ownerCompletedLendings: number;
  ownerJoinedDate: string;
  viewCount: number;
  updatedAt: string;
}

export interface BorrowRequest {
  id: number;
  itemId: number;
  itemTitle: string;
  itemImage?: string;
  itemLocation?: string;
  dailyRate?: number;
  depositAmount?: number;
  borrowerId: number;
  borrowerName: string;
  borrowerReputation: number;
  borrowerProfileImage?: string;
  ownerId: number;
  ownerName: string;
  ownerProfileImage?: string;
  startDate: string;
  endDate: string;
  totalDays: number;
  estimatedTotalCost: number;
  message: string;
  purpose?: string;
  status: RequestStatus;
  responseMessage?: string;
  cancellationReason?: string;
  createdAt: string;
}

export interface BorrowTransaction {
  id: number;
  borrowRequestId: number;
  itemId: number;
  itemTitle: string;
  itemImage?: string;
  itemLocation?: string;
  borrowerId: number;
  borrowerName: string;
  borrowerPhone?: string;
  borrowerEmail?: string;
  ownerId: number;
  ownerName: string;
  ownerPhone?: string;
  ownerEmail?: string;
  startDate: string;
  endDate: string;
  pickupCode: string;
  returnCode: string;
  depositHeld: number;
  status: TransactionStatus;
  handoverLocation?: string;
  notes?: string;
  pickupTime?: string;
  returnTime?: string;
  ownerPickupConfirmed: boolean;
  borrowerPickupConfirmed: boolean;
  ownerReturnConfirmed: boolean;
  borrowerReturnConfirmed: boolean;
  createdAt: string;
}

export interface TransactionCondition {
  id: number;
  stage: ConditionStage;
  notes: string;
  imageUrls: string[];
  recordedById: number;
  recordedByName: string;
  createdAt: string;
}

export interface Rating {
  id: number;
  transactionId: number;
  itemId: number;
  itemTitle: string;
  fromUserId: number;
  fromUserName: string;
  fromUserProfileImage?: string;
  toUserId: number;
  toUserName: string;
  score: number;
  communicationScore: number;
  punctualityScore: number;
  reliabilityScore: number;
  reviewComment?: string;
  createdAt: string;
}

export interface Notification {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  content?: string;
  actionUrl?: string;
  referenceId?: number;
  read: boolean;
  isRead?: boolean;
  createdAt: string;
}

export interface Conversation {
  id: number;
  otherUserId: number;
  otherUserName: string;
  otherUserProfileImage?: string;
  lastMessage?: string;
  lastMessageTime?: string;
  unreadCount: number;
  borrowRequestId?: number;
  transactionId?: number;
}

export interface ChatMessage {
  id: number;
  conversationId: number;
  senderId: number;
  senderName: string;
  senderProfileImage?: string;
  content: string;
  isRead: boolean;
  createdAt: string;
}

export interface Dispute {
  id: number;
  transactionId: number;
  itemId: number;
  itemTitle: string;
  createdById: number;
  createdByName: string;
  raisedByName?: string;
  againstUserId: number;
  againstUserName: string;
  reason: string;
  description: string;
  evidenceImages: string[];
  status: DisputeStatus;
  adminDecision?: string;
  resolutionNotes?: string;
  resolvedById?: number;
  resolvedByName?: string;
  resolvedAt?: string;
  createdAt: string;
}

export interface Report {
  id: number;
  reportedById: number;
  reporterName?: string;
  reportedByName?: string;
  reportedUserId?: number;
  reportedUserName?: string;
  reportedItemId?: number;
  reportedItemTitle?: string;
  reason: string;
  description: string;
  status: ReportStatus;
  adminNotes?: string;
  resolvedAt?: string;
  createdAt: string;
}

export type ContentReport = Report;

export interface AdminStats {
  totalUsers: number;
  activeUsers: number;
  verifiedUsers: number;
  totalItems: number;
  availableItems: number;
  borrowedItems: number;
  inactiveItems: number;
  totalTransactions: number;
  completedTransactions: number;
  activeTransactions: number;
  overdueTransactions: number;
  disputedTransactions: number;
  totalDepositHeld: number;
  openDisputes: number;
  openReports: number;
  categoryItemCounts: Record<string, number>;
}

export interface AdminUser {
  id: number;
  email: string;
  fullName: string;
  phone?: string;
  location?: string;
  profileImageUrl?: string;
  roles: string[];
  role?: string;
  verified: boolean;
  active: boolean;
  averageRating: number;
  ratingCount: number;
  reputationScore: number;
  completedBorrowings: number;
  completedLendings: number;
  cancellationCount: number;
  disputeCount: number;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
