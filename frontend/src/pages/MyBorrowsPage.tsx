import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRightLeft,
  Clock,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  QrCode,
  ShieldCheck,
  Star,
  Camera,
  MessageSquare,
  Sparkles,
  X,
} from 'lucide-react';
import { QRCodeSVG } from 'qrcode.react';
import { borrowService } from '../services/borrowService';
import { transactionService, LogConditionPayload } from '../services/transactionService';
import { ratingService, CreateRatingPayload } from '../services/ratingService';
import { disputeService, CreateDisputePayload } from '../services/disputeService';
import { BorrowRequest, BorrowTransaction, ConditionStage } from '../types';
import { useToast } from '../context/ToastContext';

export const MyBorrowsPage: React.FC = () => {
  const { success, error } = useToast();
  const [activeTab, setActiveTab] = useState<'requests' | 'transactions'>('transactions');

  const [requests, setRequests] = useState<BorrowRequest[]>([]);
  const [transactions, setTransactions] = useState<BorrowTransaction[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Modals state
  const [selectedTxForOtp, setSelectedTxForOtp] = useState<BorrowTransaction | null>(null);

  // Condition modal
  const [conditionModalOpen, setConditionModalOpen] = useState<boolean>(false);
  const [conditionTxId, setConditionTxId] = useState<number | null>(null);
  const [conditionStage, setConditionStage] = useState<ConditionStage>('PRE_PICKUP');
  const [conditionNotes, setConditionNotes] = useState<string>('');
  const [submittingCondition, setSubmittingCondition] = useState<boolean>(false);

  // Rating modal
  const [ratingModalOpen, setRatingModalOpen] = useState<boolean>(false);
  const [ratingTx, setRatingTx] = useState<BorrowTransaction | null>(null);
  const [overallScore, setOverallScore] = useState<number>(5);
  const [commScore, setCommScore] = useState<number>(5);
  const [punctScore, setPunctScore] = useState<number>(5);
  const [relScore, setRelScore] = useState<number>(5);
  const [reviewComment, setReviewComment] = useState<string>('');
  const [submittingRating, setSubmittingRating] = useState<boolean>(false);

  // Dispute modal
  const [disputeModalOpen, setDisputeModalOpen] = useState<boolean>(false);
  const [disputeTxId, setDisputeTxId] = useState<number | null>(null);
  const [disputeReason, setDisputeReason] = useState<string>('ITEM_DAMAGED');
  const [disputeDesc, setDisputeDesc] = useState<string>('');
  const [submittingDispute, setSubmittingDispute] = useState<boolean>(false);

  const loadData = () => {
    setLoading(true);
    Promise.all([
      borrowService.getMyBorrowRequests().catch(() => ({ content: [] })),
      transactionService.getMyBorrowTransactions().catch(() => ({ content: [] })),
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

  const handleCancelRequest = async (requestId: number) => {
    const reason = prompt('Reason for cancelling request:');
    if (!reason) return;
    try {
      await borrowService.cancelRequest(requestId, reason);
      success('Borrow request cancelled.');
      loadData();
    } catch {
      error('Failed to cancel borrow request.');
    }
  };

  const handleLogCondition = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!conditionTxId || !conditionNotes.trim()) return;

    setSubmittingCondition(true);
    try {
      await transactionService.logCondition(conditionTxId, {
        stage: conditionStage,
        notes: conditionNotes.trim(),
        imageUrls: [],
      });
      success('Condition check recorded successfully!');
      setConditionModalOpen(false);
      setConditionNotes('');
    } catch {
      error('Failed to log condition check.');
    } finally {
      setSubmittingCondition(false);
    }
  };

  const handleSubmitRating = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ratingTx) return;

    setSubmittingRating(true);
    try {
      await ratingService.submitRating({
        transactionId: ratingTx.id,
        score: overallScore,
        communicationScore: commScore,
        punctualityScore: punctScore,
        reliabilityScore: relScore,
        reviewComment: reviewComment.trim() || undefined,
      });
      success('Review and rating submitted! Thank you for strengthening community trust.');
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
      success('Dispute filed. Platform administrators are reviewing the case.');
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
    BORROWED: { bg: 'bg-teal-950/60 border-teal-500/30 text-teal-300', text: 'Active Borrowing' },
    RETURN_PENDING: { bg: 'bg-amber-950/60 border-amber-500/30 text-amber-300', text: 'Return Pending' },
    COMPLETED: { bg: 'bg-slate-800 border-slate-700 text-slate-300', text: 'Completed' },
    OVERDUE: { bg: 'bg-rose-950/80 border-rose-500/50 text-rose-300', text: 'OVERDUE' },
    DISPUTED: { bg: 'bg-purple-950/80 border-purple-500/40 text-purple-300', text: 'Disputed' },
    PENDING: { bg: 'bg-amber-950/60 border-amber-500/30 text-amber-300', text: 'Pending Approval' },
    ACCEPTED: { bg: 'bg-emerald-950/60 border-emerald-500/30 text-emerald-300', text: 'Accepted' },
    REJECTED: { bg: 'bg-rose-950/60 border-rose-500/30 text-rose-300', text: 'Rejected' },
    CANCELLED: { bg: 'bg-slate-800 border-slate-700 text-slate-400', text: 'Cancelled' },
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8 lg:py-12">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight">My Borrowings</h1>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Track your item requests, active OTP verification codes, and return deadlines.
            </p>
          </div>

          {/* Tab buttons */}
          <div className="flex items-center bg-slate-900 p-1 rounded-xl border border-slate-800 self-start">
            <button
              onClick={() => setActiveTab('transactions')}
              className={`px-4 py-2 rounded-lg text-xs font-semibold transition-colors ${
                activeTab === 'transactions'
                  ? 'bg-emerald-500 text-slate-950 shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Active Bookings ({transactions.length})
            </button>
            <button
              onClick={() => setActiveTab('requests')}
              className={`px-4 py-2 rounded-lg text-xs font-semibold transition-colors ${
                activeTab === 'requests'
                  ? 'bg-emerald-500 text-slate-950 shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Pending Requests ({requests.length})
            </button>
          </div>
        </div>

        {/* Content */}
        {loading ? (
          <div className="text-center py-20 text-slate-500">Loading your borrowing records...</div>
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
                        Booking #{tx.id}
                      </span>
                    </div>

                    <h3 className="text-xl font-bold text-white hover:text-emerald-400 transition-colors">
                      <Link to={`/items/${tx.itemId}`}>{tx.itemTitle}</Link>
                    </h3>

                    <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400">
                      <span>Owner: <strong className="text-slate-200">{tx.ownerName}</strong></span>
                      <span>Dates: <strong className="text-slate-200">{tx.startDate} to {tx.endDate}</strong></span>
                      <span>Deposit: <strong className="text-emerald-400">₹{tx.depositHeld}</strong></span>
                    </div>
                  </div>

                  {/* Actions Column */}
                  <div className="flex flex-wrap items-center gap-2 self-stretch md:self-auto justify-end">
                    {/* View OTP code button */}
                    {(tx.status === 'READY_FOR_PICKUP' || tx.status === 'BORROWED' || tx.status === 'UPCOMING') && (
                      <button
                        onClick={() => setSelectedTxForOtp(tx)}
                        className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded-xl text-xs flex items-center gap-1.5 shadow-md shadow-emerald-500/20"
                      >
                        <QrCode className="w-4 h-4" />
                        Show Handover OTP
                      </button>
                    )}

                    {/* Condition logger */}
                    {tx.status === 'BORROWED' && (
                      <button
                        onClick={() => {
                          setConditionTxId(tx.id);
                          setConditionModalOpen(true);
                        }}
                        className="px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl"
                      >
                        <Camera className="w-4 h-4 text-cyan-400 inline mr-1" />
                        Log Condition
                      </button>
                    )}

                    {/* Review Button */}
                    {tx.status === 'COMPLETED' && (
                      <button
                        onClick={() => {
                          setRatingTx(tx);
                          setRatingModalOpen(true);
                        }}
                        className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold rounded-xl text-xs flex items-center gap-1.5 shadow-md shadow-amber-500/20"
                      >
                        <Star className="w-4 h-4 fill-slate-950" />
                        Leave 4D Review
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
                        Report Issue / Dispute
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-20 bg-slate-900/40 border border-slate-800 rounded-3xl space-y-3">
              <ArrowRightLeft className="w-12 h-12 text-slate-600 mx-auto" />
              <h3 className="text-lg font-bold text-white">No active bookings yet</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">
                Explore gear from your neighbors and borrow what you need for short-term projects.
              </p>
              <Link
                to="/explore"
                className="inline-block px-5 py-2.5 bg-emerald-500 text-slate-950 text-xs font-bold rounded-xl"
              >
                Browse Items
              </Link>
            </div>
          )
        ) : (
          /* Tab 2: Requests */
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
                      <span>Owner: <strong className="text-slate-200">{req.ownerName}</strong></span>
                      <span>Requested Dates: <strong className="text-slate-200">{req.startDate} to {req.endDate}</strong> ({req.totalDays} days)</span>
                      <span>Est. Total: <strong className="text-emerald-400">₹{req.estimatedTotalCost}</strong></span>
                    </div>

                    <p className="text-xs text-slate-400 italic bg-slate-950/60 p-2.5 rounded-xl border border-slate-800/60">
                      "{req.message}"
                    </p>
                  </div>

                  {req.status === 'PENDING' && (
                    <button
                      onClick={() => handleCancelRequest(req.id)}
                      className="px-4 py-2 bg-slate-800 hover:bg-rose-950 text-slate-400 hover:text-rose-300 text-xs font-semibold rounded-xl transition-colors"
                    >
                      Cancel Request
                    </button>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-20 bg-slate-900/40 border border-slate-800 rounded-3xl space-y-3">
              <Clock className="w-12 h-12 text-slate-600 mx-auto" />
              <h3 className="text-lg font-bold text-white">No pending requests</h3>
              <p className="text-xs text-slate-400">All your requests will appear here for tracking.</p>
            </div>
          )
        )}
      </div>

      {/* OTP Code & QR Modal */}
      {selectedTxForOtp && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-sm w-full p-6 text-center space-y-6 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-base font-bold text-white">Handover Security Verification</h3>
              <button
                onClick={() => setSelectedTxForOtp(null)}
                className="p-1 text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3">
              <div className="text-xs text-slate-400">
                Show this 6-digit code or QR to the owner when you meet in person:
              </div>

              {/* Pickup OTP */}
              <div className="p-4 bg-slate-950 border border-emerald-500/40 rounded-2xl">
                <div className="text-[11px] font-bold text-emerald-400 uppercase tracking-wider mb-1">
                  Pickup Handover Code
                </div>
                <div className="text-4xl font-black text-white tracking-widest font-mono">
                  {selectedTxForOtp.pickupCode}
                </div>
              </div>

              {/* QR Code */}
              <div className="p-4 bg-white rounded-2xl flex items-center justify-center inline-block mx-auto shadow-lg">
                <QRCodeSVG value={`BORROWBOX:PICKUP:${selectedTxForOtp.id}:${selectedTxForOtp.pickupCode}`} size={160} />
              </div>

              {/* Return OTP */}
              <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl text-xs">
                <div className="text-slate-500 mb-0.5">Return Handover Code</div>
                <div className="text-lg font-mono font-bold text-slate-200 tracking-wider">
                  {selectedTxForOtp.returnCode}
                </div>
              </div>
            </div>

            <p className="text-[11px] text-slate-500 leading-relaxed">
              Once the owner inputs your code, the handover status updates immediately and deposit is logged.
            </p>
          </div>
        </div>
      )}

      {/* 4D Rating Modal */}
      {ratingModalOpen && ratingTx && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-lg w-full p-6 sm:p-8 space-y-6 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <h3 className="text-lg font-bold text-white">Review & Reputation Rating</h3>
                <p className="text-xs text-slate-400 mt-0.5">{ratingTx.itemTitle} • {ratingTx.ownerName}</p>
              </div>
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
                  Overall Score (1-5): {overallScore} ⭐
                </label>
                <input
                  type="range"
                  min="1"
                  max="5"
                  value={overallScore}
                  onChange={(e) => setOverallScore(Number(e.target.value))}
                  className="w-full accent-emerald-500"
                />
              </div>

              <div className="grid grid-cols-3 gap-3 pt-2">
                <div>
                  <label className="font-medium text-slate-400 block mb-1">Communication</label>
                  <select
                    value={commScore}
                    onChange={(e) => setCommScore(Number(e.target.value))}
                    className="w-full p-2 bg-slate-950 border border-slate-800 rounded-xl text-white"
                  >
                    {[5, 4, 3, 2, 1].map((s) => (
                      <option key={s} value={s}>{s} Stars</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="font-medium text-slate-400 block mb-1">Punctuality</label>
                  <select
                    value={punctScore}
                    onChange={(e) => setPunctScore(Number(e.target.value))}
                    className="w-full p-2 bg-slate-950 border border-slate-800 rounded-xl text-white"
                  >
                    {[5, 4, 3, 2, 1].map((s) => (
                      <option key={s} value={s}>{s} Stars</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="font-medium text-slate-400 block mb-1">Condition Match</label>
                  <select
                    value={relScore}
                    onChange={(e) => setRelScore(Number(e.target.value))}
                    className="w-full p-2 bg-slate-950 border border-slate-800 rounded-xl text-white"
                  >
                    {[5, 4, 3, 2, 1].map((s) => (
                      <option key={s} value={s}>{s} Stars</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="font-semibold text-slate-300 block mb-1">Review Comment</label>
                <textarea
                  rows={3}
                  placeholder="Share feedback on equipment functionality, owner responsiveness, and handoff experience..."
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  className="w-full p-3 bg-slate-950 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
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
                  className="px-6 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded-xl shadow-md"
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
                File Transaction Dispute
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
                <label className="font-semibold text-slate-300 block mb-1">Reason</label>
                <select
                  value={disputeReason}
                  onChange={(e) => setDisputeReason(e.target.value)}
                  className="w-full p-2 bg-slate-950 border border-slate-800 rounded-xl text-white"
                >
                  <option value="ITEM_DAMAGED">Item Damaged / Malfunctioning</option>
                  <option value="ITEM_NOT_RETURNED">Item Not Returned</option>
                  <option value="MISLEADING_CONDITION">Condition did not match description</option>
                  <option value="SECURITY_DEPOSIT_DISAGREEMENT">Security deposit deduction dispute</option>
                </select>
              </div>

              <div>
                <label className="font-semibold text-slate-300 block mb-1">Description & Evidence Details</label>
                <textarea
                  rows={3}
                  required
                  placeholder="Explain what happened in detail..."
                  value={disputeDesc}
                  onChange={(e) => setDisputeDesc(e.target.value)}
                  className="w-full p-3 bg-slate-950 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-rose-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
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
                  {submittingDispute ? 'Filing...' : 'File Dispute'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
