import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Share2,
  CheckCircle2,
  XCircle,
  ShieldCheck,
  KeyRound,
  AlertTriangle,
  Camera,
  Star,
  X,
  Clock,
  Coins,
} from 'lucide-react';
import { borrowService } from '../services/borrowService';
import { transactionService } from '../services/transactionService';
import { ratingService } from '../services/ratingService';
import { disputeService } from '../services/disputeService';
import { BorrowRequest, BorrowTransaction, ConditionStage } from '../types';
import { useToast } from '../context/ToastContext';

export const MyLendsPage: React.FC = () => {
  const { success, error } = useToast();
  const [activeTab, setActiveTab] = useState<'requests' | 'transactions'>('transactions');

  const [requests, setRequests] = useState<BorrowRequest[]>([]);
  const [transactions, setTransactions] = useState<BorrowTransaction[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Handover Verification Modal
  const [verifyModalOpen, setVerifyModalOpen] = useState<boolean>(false);
  const [verifyType, setVerifyType] = useState<'PICKUP' | 'RETURN'>('PICKUP');
  const [verifyTxId, setVerifyTxId] = useState<number | null>(null);
  const [inputOtp, setInputOtp] = useState<string>('');
  const [verifyNotes, setVerifyNotes] = useState<string>('');
  const [submittingVerify, setSubmittingVerify] = useState<boolean>(false);

  // Rating Modal for owner rating borrower
  const [ratingModalOpen, setRatingModalOpen] = useState<boolean>(false);
  const [ratingTx, setRatingTx] = useState<BorrowTransaction | null>(null);
  const [score, setScore] = useState<number>(5);
  const [reviewComment, setReviewComment] = useState<string>('');
  const [submittingRating, setSubmittingRating] = useState<boolean>(false);

  // Dispute Modal
  const [disputeModalOpen, setDisputeModalOpen] = useState<boolean>(false);
  const [disputeTxId, setDisputeTxId] = useState<number | null>(null);
  const [disputeReason, setDisputeReason] = useState<string>('ITEM_DAMAGED');
  const [disputeDesc, setDisputeDesc] = useState<string>('');
  const [submittingDispute, setSubmittingDispute] = useState<boolean>(false);

  const loadData = () => {
    setLoading(true);
    Promise.all([
      borrowService.getMyLendingRequests().catch(() => ({ content: [] })),
      transactionService.getMyLendTransactions().catch(() => ({ content: [] })),
    ])
      .then(([reqs, txs]) => {
        setRequests(reqs.content);
        setTransactions(txs.content);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleAcceptRequest = async (requestId: number) => {
    const responseMsg = prompt('Optional welcome message / pickup instructions for borrower:');
    try {
      await borrowService.acceptRequest(requestId, responseMsg || undefined);
      success('Borrow request accepted! Booking initialized.');
      loadData();
    } catch (err: any) {
      error(err.response?.data?.message || 'Failed to accept request.');
    }
  };

  const handleRejectRequest = async (requestId: number) => {
    const reason = prompt('Reason for declining request:');
    if (!reason) return;
    try {
      await borrowService.rejectRequest(requestId, reason);
      success('Borrow request rejected.');
      loadData();
    } catch {
      error('Failed to reject request.');
    }
  };

  const handleConfirmOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!verifyTxId || inputOtp.length !== 6) {
      error('Please enter the valid 6-digit OTP code provided by the borrower.');
      return;
    }

    setSubmittingVerify(true);
    try {
      if (verifyType === 'PICKUP') {
        await transactionService.verifyPickup(verifyTxId, {
          pickupCode: inputOtp.trim(),
          notes: verifyNotes.trim() || undefined,
        });
        success('Pickup verified! Handover completed and item is now in active borrowing status.');
      } else {
        await transactionService.verifyReturn(verifyTxId, {
          returnCode: inputOtp.trim(),
          notes: verifyNotes.trim() || undefined,
        });
        success('Return verified! Item returned safely, deposit released, and transaction completed.');
      }
      setVerifyModalOpen(false);
      setInputOtp('');
      setVerifyNotes('');
      loadData();
    } catch (err: any) {
      error(err.response?.data?.message || 'Invalid 6-digit verification code.');
    } finally {
      setSubmittingVerify(false);
    }
  };

  const handleSubmitRating = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ratingTx) return;

    setSubmittingRating(true);
    try {
      await ratingService.submitRating({
        transactionId: ratingTx.id,
        score,
        communicationScore: score,
        punctualityScore: score,
        reliabilityScore: score,
        reviewComment: reviewComment.trim() || undefined,
      });
      success('Borrower review submitted!');
      setRatingModalOpen(false);
      loadData();
    } catch (err: any) {
      error(err.response?.data?.message || 'Failed to submit rating.');
    } finally {
      setSubmittingRating(false);
    }
  };

  const handleFileDispute = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!disputeTxId || !disputeDesc.trim()) return;

    setSubmittingDispute(true);
    try {
      await disputeService.createDispute({
        transactionId: disputeTxId,
        reason: disputeReason,
        description: disputeDesc.trim(),
      });
      success('Dispute filed. Admin will review the transaction and evidence.');
      setDisputeModalOpen(false);
      setDisputeDesc('');
      loadData();
    } catch (err: any) {
      error(err.response?.data?.message || 'Failed to file dispute.');
    } finally {
      setSubmittingDispute(false);
    }
  };

  const statusBadges: Record<string, { bg: string; text: string }> = {
    UPCOMING: { bg: 'bg-blue-950/60 border-blue-500/30 text-blue-300', text: 'Upcoming' },
    READY_FOR_PICKUP: { bg: 'bg-emerald-950/60 border-emerald-500/30 text-emerald-300', text: 'Ready for Pickup' },
    BORROWED: { bg: 'bg-teal-950/60 border-teal-500/30 text-teal-300', text: 'Out on Borrow' },
    RETURN_PENDING: { bg: 'bg-amber-950/60 border-amber-500/30 text-amber-300', text: 'Return Pending' },
    COMPLETED: { bg: 'bg-slate-800 border-slate-700 text-slate-300', text: 'Completed & Returned' },
    OVERDUE: { bg: 'bg-rose-950/80 border-rose-500/50 text-rose-300', text: 'OVERDUE' },
    DISPUTED: { bg: 'bg-purple-950/80 border-purple-500/40 text-purple-300', text: 'Disputed' },
    PENDING: { bg: 'bg-amber-950/60 border-amber-500/30 text-amber-300', text: 'Pending Your Approval' },
    ACCEPTED: { bg: 'bg-emerald-950/60 border-emerald-500/30 text-emerald-300', text: 'Accepted' },
    REJECTED: { bg: 'bg-rose-950/60 border-rose-500/30 text-rose-300', text: 'Rejected' },
    CANCELLED: { bg: 'bg-slate-800 border-slate-700 text-slate-400', text: 'Cancelled' },
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8 lg:py-12">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight">My Lendings</h1>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Approve borrower bookings, verify handover OTPs, and track your active equipment.
            </p>
          </div>

          <div className="flex items-center bg-slate-900 p-1 rounded-xl border border-slate-800 self-start">
            <button
              onClick={() => setActiveTab('transactions')}
              className={`px-4 py-2 rounded-lg text-xs font-semibold transition-colors ${
                activeTab === 'transactions'
                  ? 'bg-emerald-500 text-slate-950 shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Active Lendings ({transactions.length})
            </button>
            <button
              onClick={() => setActiveTab('requests')}
              className={`px-4 py-2 rounded-lg text-xs font-semibold transition-colors ${
                activeTab === 'requests'
                  ? 'bg-emerald-500 text-slate-950 shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Incoming Requests ({requests.filter((r) => r.status === 'PENDING').length})
            </button>
          </div>
        </div>

        {/* Content */}
        {loading ? (
          <div className="text-center py-20 text-slate-500">Loading lending records...</div>
        ) : activeTab === 'transactions' ? (
          transactions.length > 0 ? (
            <div className="space-y-4">
              {transactions.map((tx) => (
                <div
                  key={tx.id}
                  className="p-6 bg-slate-900/70 border border-slate-800/80 rounded-3xl flex flex-col md:flex-row items-start md:items-center justify-between gap-6 hover:border-slate-700 transition-colors"
                >
                  <div className="space-y-2 flex-1">
                    <div className="flex items-center gap-3">
                      <span
                        className={`px-3 py-0.5 text-xs font-bold rounded-full border uppercase tracking-wider ${
                          statusBadges[tx.status]?.bg || 'bg-slate-800'
                        }`}
                      >
                        {statusBadges[tx.status]?.text || tx.status}
                      </span>
                      <span className="text-xs text-slate-500 font-mono">
                        Lending #{tx.id}
                      </span>
                    </div>

                    <h3 className="text-xl font-bold text-white hover:text-emerald-400 transition-colors">
                      <Link to={`/items/${tx.itemId}`}>{tx.itemTitle}</Link>
                    </h3>

                    <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400">
                      <span>Borrower: <strong className="text-slate-200">{tx.borrowerName}</strong></span>
                      <span>Booking: <strong className="text-slate-200">{tx.startDate} to {tx.endDate}</strong></span>
                      <span>Deposit Held: <strong className="text-emerald-400">₹{tx.depositHeld}</strong></span>
                    </div>
                  </div>

                  {/* Actions Column */}
                  <div className="flex flex-wrap items-center gap-2 self-stretch md:self-auto justify-end">
                    {/* Enter Pickup OTP */}
                    {(tx.status === 'READY_FOR_PICKUP' || tx.status === 'UPCOMING') && (
                      <button
                        onClick={() => {
                          setVerifyTxId(tx.id);
                          setVerifyType('PICKUP');
                          setVerifyModalOpen(true);
                        }}
                        className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded-xl text-xs flex items-center gap-1.5 shadow-md shadow-emerald-500/20"
                      >
                        <KeyRound className="w-4 h-4" />
                        Verify Pickup OTP
                      </button>
                    )}

                    {/* Enter Return OTP */}
                    {(tx.status === 'BORROWED' || tx.status === 'RETURN_PENDING' || tx.status === 'OVERDUE') && (
                      <button
                        onClick={() => {
                          setVerifyTxId(tx.id);
                          setVerifyType('RETURN');
                          setVerifyModalOpen(true);
                        }}
                        className="px-4 py-2 bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold rounded-xl text-xs flex items-center gap-1.5 shadow-md shadow-teal-500/20"
                      >
                        <KeyRound className="w-4 h-4" />
                        Verify Return OTP
                      </button>
                    )}

                    {/* Leave Review */}
                    {tx.status === 'COMPLETED' && (
                      <button
                        onClick={() => {
                          setRatingTx(tx);
                          setRatingModalOpen(true);
                        }}
                        className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold rounded-xl text-xs flex items-center gap-1.5 shadow-md shadow-amber-500/20"
                      >
                        <Star className="w-4 h-4 fill-slate-950" />
                        Rate Borrower
                      </button>
                    )}

                    {/* Dispute button */}
                    {(tx.status === 'BORROWED' || tx.status === 'OVERDUE') && (
                      <button
                        onClick={() => {
                          setDisputeTxId(tx.id);
                          setDisputeModalOpen(true);
                        }}
                        className="px-3 py-2 bg-slate-800 hover:bg-rose-950 text-slate-400 hover:text-rose-400 text-xs font-semibold rounded-xl transition-colors"
                      >
                        Report Damage / Issue
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-20 bg-slate-900/40 border border-slate-800 rounded-3xl space-y-3">
              <Share2 className="w-12 h-12 text-slate-600 mx-auto" />
              <h3 className="text-lg font-bold text-white">No active lendings</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">
                Items you list will appear here once neighbors request to borrow them.
              </p>
              <Link
                to="/items/create"
                className="inline-block px-5 py-2.5 bg-emerald-500 text-slate-950 text-xs font-bold rounded-xl"
              >
                List Gear for Lending
              </Link>
            </div>
          )
        ) : (
          /* Tab 2: Inbound Requests */
          requests.length > 0 ? (
            <div className="space-y-4">
              {requests.map((req) => (
                <div
                  key={req.id}
                  className="p-6 bg-slate-900/70 border border-slate-800/80 rounded-3xl flex flex-col md:flex-row items-start md:items-center justify-between gap-6"
                >
                  <div className="space-y-2 flex-1">
                    <div className="flex items-center gap-3">
                      <span
                        className={`px-3 py-0.5 text-xs font-bold rounded-full border uppercase tracking-wider ${
                          statusBadges[req.status]?.bg || 'bg-slate-800'
                        }`}
                      >
                        {statusBadges[req.status]?.text || req.status}
                      </span>
                      <span className="text-xs text-slate-500 font-mono">
                        Request #{req.id}
                      </span>
                    </div>

                    <h3 className="text-xl font-bold text-white">
                      <Link to={`/items/${req.itemId}`}>{req.itemTitle}</Link>
                    </h3>

                    <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400">
                      <div className="flex items-center gap-1.5">
                        <span className="text-slate-400">Borrower:</span>
                        <strong className="text-slate-200">{req.borrowerName}</strong>
                        <span className="px-1.5 py-0.2 bg-emerald-950 border border-emerald-500/20 text-emerald-400 text-[10px] rounded font-mono">
                          ⭐ {req.borrowerReputation || 80} Rep
                        </span>
                      </div>
                      <span>Dates: <strong className="text-slate-200">{req.startDate} to {req.endDate}</strong> ({req.totalDays} days)</span>
                      <span>Total Earnings: <strong className="text-emerald-400">₹{req.estimatedTotalCost}</strong></span>
                    </div>

                    <p className="text-xs text-slate-300 italic bg-slate-950/60 p-3 rounded-xl border border-slate-800/60">
                      "{req.message}"
                    </p>
                  </div>

                  {req.status === 'PENDING' && (
                    <div className="flex items-center gap-3 self-stretch md:self-auto">
                      <button
                        onClick={() => handleRejectRequest(req.id)}
                        className="flex-1 md:flex-none px-4 py-2.5 bg-slate-800 hover:bg-rose-950 text-slate-400 hover:text-rose-300 text-xs font-semibold rounded-xl transition-colors"
                      >
                        Decline
                      </button>
                      <button
                        onClick={() => handleAcceptRequest(req.id)}
                        className="flex-1 md:flex-none px-6 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold rounded-xl shadow-lg shadow-emerald-500/20 transition-all"
                      >
                        Accept & Approve
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-20 bg-slate-900/40 border border-slate-800 rounded-3xl space-y-3">
              <Clock className="w-12 h-12 text-slate-600 mx-auto" />
              <h3 className="text-lg font-bold text-white">No incoming borrow requests</h3>
              <p className="text-xs text-slate-400">New requests from borrowers will appear here for review.</p>
            </div>
          )
        )}
      </div>

      {/* Handover OTP Verification Modal */}
      {verifyModalOpen && verifyTxId && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 sm:p-8 space-y-6 shadow-2xl text-center">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-emerald-400" />
                {verifyType === 'PICKUP' ? 'Verify Pickup Handover' : 'Verify Return Handover'}
              </h3>
              <button
                onClick={() => setVerifyModalOpen(false)}
                className="p-1 text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-slate-400 leading-relaxed">
              Ask the borrower for their 6-digit handover code generated in their BorrowBox account to complete verification:
            </p>

            <form onSubmit={handleConfirmOtp} className="space-y-4">
              <div>
                <input
                  type="text"
                  maxLength={6}
                  required
                  placeholder="0 0 0 0 0 0"
                  value={inputOtp}
                  onChange={(e) => setInputOtp(e.target.value.replace(/\D/g, ''))}
                  className="w-full text-center text-3xl font-mono font-black tracking-widest px-4 py-3 bg-slate-950 border border-emerald-500/40 rounded-2xl text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <input
                  type="text"
                  placeholder="Optional handover notes (e.g. Clean condition, full battery)"
                  value={verifyNotes}
                  onChange={(e) => setVerifyNotes(e.target.value)}
                  className="w-full px-3.5 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white"
                />
              </div>

              <button
                type="submit"
                disabled={submittingVerify || inputOtp.length !== 6}
                className="w-full py-3.5 bg-emerald-500 hover:bg-emerald-400 disabled:opacity-40 text-slate-950 font-bold rounded-xl text-sm shadow-lg shadow-emerald-500/25"
              >
                {submittingVerify ? 'Verifying OTP...' : 'Confirm Handover'}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Rating Modal */}
      {ratingModalOpen && ratingTx && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-base font-bold text-white">Rate Borrower: {ratingTx.borrowerName}</h3>
              <button
                onClick={() => setRatingModalOpen(false)}
                className="p-1 text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmitRating} className="space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-300 block mb-1">
                  Rating (1-5): {score} ⭐
                </label>
                <input
                  type="range"
                  min="1"
                  max="5"
                  value={score}
                  onChange={(e) => setScore(Number(e.target.value))}
                  className="w-full accent-emerald-500"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-300 block mb-1">Feedback Comment</label>
                <textarea
                  rows={3}
                  placeholder="How was the borrower's punctuality and care for your equipment?"
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  className="w-full p-3 bg-slate-950 border border-slate-800 rounded-xl text-white"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setRatingModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 font-semibold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingRating}
                  className="px-5 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded-xl"
                >
                  {submittingRating ? 'Submitting...' : 'Submit Rating'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Dispute Modal */}
      {disputeModalOpen && disputeTxId && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-base font-bold text-white flex items-center gap-2 text-rose-400">
                <AlertTriangle className="w-5 h-5" />
                Report Equipment Damage / Issue
              </h3>
              <button
                onClick={() => setDisputeModalOpen(false)}
                className="p-1 text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleFileDispute} className="space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-300 block mb-1">Issue Type</label>
                <select
                  value={disputeReason}
                  onChange={(e) => setDisputeReason(e.target.value)}
                  className="w-full p-2 bg-slate-950 border border-slate-800 rounded-xl text-white"
                >
                  <option value="ITEM_DAMAGED">Equipment Returned Damaged</option>
                  <option value="ITEM_NOT_RETURNED">Item Overdue / Not Returned</option>
                  <option value="MISSING_PARTS">Accessories or Parts Missing</option>
                  <option value="SECURITY_DEPOSIT_DISAGREEMENT">Security Deposit Claim</option>
                </select>
              </div>

              <div>
                <label className="font-semibold text-slate-300 block mb-1">Description & Evidence</label>
                <textarea
                  rows={3}
                  required
                  placeholder="Describe damage details and estimated repair cost..."
                  value={disputeDesc}
                  onChange={(e) => setDisputeDesc(e.target.value)}
                  className="w-full p-3 bg-slate-950 border border-slate-800 rounded-xl text-white"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setDisputeModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 font-semibold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingDispute}
                  className="px-5 py-2 bg-rose-600 hover:bg-rose-500 text-white font-bold rounded-xl"
                >
                  {submittingDispute ? 'Filing...' : 'File Claim'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
