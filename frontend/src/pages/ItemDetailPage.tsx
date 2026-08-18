import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import {
  MapPin,
  ShieldCheck,
  Calendar,
  Lock,
  Heart,
  MessageSquare,
  AlertTriangle,
  Flag,
  CheckCircle2,
  Sparkles,
  Box,
  Clock,
  Coins,
  X,
  Share2,
} from 'lucide-react';
import { itemService } from '../services/itemService';
import { borrowService, AvailabilityCheckResult } from '../services/borrowService';
import { favoriteService } from '../services/favoriteService';
import { ratingService } from '../services/ratingService';
import { disputeService } from '../services/disputeService';
import { chatService } from '../services/chatService';
import { ItemDetail, Rating } from '../types';
import { RatingStars } from '../components/RatingStars';
import { AvailabilityCalendar } from '../components/AvailabilityCalendar';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export const ItemDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const { success, error, info } = useToast();

  const [item, setItem] = useState<ItemDetail | null>(null);
  const [activeImageIndex, setActiveImageIndex] = useState<number>(0);
  const [ratings, setRatings] = useState<Rating[]>([]);
  const [isFavorited, setIsFavorited] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);

  // Booking selection state
  const [startDate, setStartDate] = useState<string | null>(null);
  const [endDate, setEndDate] = useState<string | null>(null);
  const [availabilityResult, setAvailabilityResult] = useState<AvailabilityCheckResult | null>(null);
  const [checkingAvailability, setCheckingAvailability] = useState<boolean>(false);

  // Modals
  const [borrowModalOpen, setBorrowModalOpen] = useState<boolean>(false);
  const [borrowPurpose, setBorrowPurpose] = useState<string>('');
  const [borrowMessage, setBorrowMessage] = useState<string>('');
  const [submittingBorrow, setSubmittingBorrow] = useState<boolean>(false);

  const [reportModalOpen, setReportModalOpen] = useState<boolean>(false);
  const [reportReason, setReportReason] = useState<string>('INAPPROPRIATE_CONTENT');
  const [reportDescription, setReportDescription] = useState<string>('');
  const [submittingReport, setSubmittingReport] = useState<boolean>(false);

  const itemId = Number(id);

  useEffect(() => {
    if (!itemId) return;
    setLoading(true);

    itemService
      .getItemById(itemId)
      .then((data) => {
        setItem(data);
        if (data.ownerId) {
          ratingService.getUserRatings(data.ownerId, 0, 5).then((res) => {
            setRatings(res.content);
          }).catch(() => {});
        }
      })
      .catch(() => {
        error('Item not found or unavailable.');
        navigate('/explore');
      })
      .finally(() => setLoading(false));

    if (isAuthenticated) {
      favoriteService
        .getFavoriteStatus(itemId)
        .then((res) => setIsFavorited(res.isFavorited))
        .catch(() => {});
    }
  }, [itemId, isAuthenticated]);

  const handleDateRangeSelect = async (start: string | null, end: string | null) => {
    setStartDate(start);
    setEndDate(end);

    if (start && end) {
      setCheckingAvailability(true);
      try {
        const result = await borrowService.checkAvailability(itemId, start, end);
        setAvailabilityResult(result);
      } catch (err: any) {
        error(err.response?.data?.message || 'Dates are not available.');
        setAvailabilityResult(null);
      } finally {
        setCheckingAvailability(false);
      }
    } else {
      setAvailabilityResult(null);
    }
  };

  const handleFavoriteToggle = async () => {
    if (!isAuthenticated) {
      error('Please sign in to save items to your watchlist.');
      return;
    }
    try {
      const res = await favoriteService.toggleFavorite(itemId);
      setIsFavorited(res.isFavorited);
      if (res.isFavorited) {
        success('Item saved to your watchlist!');
      } else {
        info('Item removed from your watchlist.');
      }
    } catch {
      error('Failed to update watchlist.');
    }
  };

  const handleStartChat = async () => {
    if (!isAuthenticated) {
      error('Please sign in to message the owner.');
      return;
    }
    if (item?.ownerId === user?.id) {
      info('You are the owner of this item.');
      return;
    }
    try {
      const conv = await chatService.startConversation({
        recipientId: item!.ownerId,
        initialMessage: `Hi ${item?.ownerName}, I am interested in borrowing '${item?.title}'.`,
      });
      navigate(`/messages?conversationId=${conv.id}`);
    } catch {
      error('Failed to start conversation.');
    }
  };

  const handleSubmitBorrowRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!startDate || !endDate) {
      error('Please select both start and return dates on the calendar.');
      return;
    }
    if (!borrowMessage.trim()) {
      error('Please include a brief message introducing yourself to the owner.');
      return;
    }

    setSubmittingBorrow(true);
    try {
      await borrowService.createBorrowRequest({
        itemId,
        startDate,
        endDate,
        purpose: borrowPurpose.trim() || undefined,
        message: borrowMessage.trim(),
      });
      success('Borrow request sent successfully! The owner will review your booking terms.');
      setBorrowModalOpen(false);
      navigate('/borrows');
    } catch (err: any) {
      error(err.response?.data?.message || 'Failed to submit borrow request.');
    } finally {
      setSubmittingBorrow(false);
    }
  };

  const handleSubmitReport = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reportDescription.trim()) {
      error('Please provide a description for the report.');
      return;
    }

    setSubmittingReport(true);
    try {
      await disputeService.createReport({
        reportedItemId: itemId,
        reason: reportReason,
        description: reportDescription.trim(),
      });
      success('Thank you. Our moderation team has received your report.');
      setReportModalOpen(false);
      setReportDescription('');
    } catch {
      error('Failed to submit report.');
    } finally {
      setSubmittingReport(false);
    }
  };

  if (loading || !item) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="w-10 h-10 border-4 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin" />
      </div>
    );
  }

  const isOwner = user?.id === item.ownerId;
  const activeImage = item.images[activeImageIndex]?.imageUrl || item.primaryImageUrl;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8 lg:py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Breadcrumb Header */}
        <div className="flex items-center justify-between text-xs text-slate-400 mb-6">
          <div className="flex items-center gap-2">
            <Link to="/explore" className="hover:text-white">Explore</Link>
            <span>/</span>
            <Link to={`/explore?category=${item.categorySlug}`} className="text-emerald-400 hover:underline">
              {item.categoryName}
            </Link>
            <span>/</span>
            <span className="text-slate-300 truncate max-w-[200px]">{item.title}</span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                navigator.clipboard.writeText(window.location.href);
                success('Link copied to clipboard!');
              }}
              className="p-2 hover:bg-slate-900 border border-slate-800 rounded-xl text-slate-400 hover:text-white transition-colors"
              title="Share listing"
            >
              <Share2 className="w-4 h-4" />
            </button>
            <button
              onClick={() => setReportModalOpen(true)}
              className="p-2 hover:bg-slate-900 border border-slate-800 rounded-xl text-slate-400 hover:text-rose-400 transition-colors"
              title="Report item"
            >
              <Flag className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Main Grid: Gallery + Details / Booking Widget */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 lg:gap-12">
          {/* Left 2 Cols: Photo Gallery & Item Info */}
          <div className="lg:col-span-2 space-y-8">
            {/* Gallery */}
            <div className="space-y-3">
              <div className="relative aspect-[16/10] w-full bg-slate-900 rounded-3xl overflow-hidden border border-slate-800 shadow-2xl">
                {activeImage ? (
                  <img
                    src={activeImage.startsWith('http') ? activeImage : `http://localhost:8080${activeImage}`}
                    alt={item.title}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-slate-700">
                    <Box className="w-16 h-16" />
                  </div>
                )}
                <div className="absolute top-4 left-4">
                  <span className="px-3 py-1 bg-emerald-500/90 text-slate-950 font-bold text-xs rounded-full uppercase tracking-wider backdrop-blur-md">
                    {item.condition.replace('_', ' ')}
                  </span>
                </div>
              </div>

              {/* Thumbnails */}
              {item.images && item.images.length > 1 && (
                <div className="flex items-center gap-3 overflow-x-auto pb-2">
                  {item.images.map((img, idx) => (
                    <button
                      key={img.id}
                      onClick={() => setActiveImageIndex(idx)}
                      className={`relative w-20 h-20 rounded-xl overflow-hidden border-2 transition-all shrink-0 ${
                        activeImageIndex === idx
                          ? 'border-emerald-500 shadow-md shadow-emerald-500/30 scale-105'
                          : 'border-slate-800 opacity-60 hover:opacity-100'
                      }`}
                    >
                      <img
                        src={img.imageUrl.startsWith('http') ? img.imageUrl : `http://localhost:8080${img.imageUrl}`}
                        alt={`Thumbnail ${idx + 1}`}
                        className="w-full h-full object-cover"
                      />
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Title & Core Details */}
            <div className="p-6 sm:p-8 bg-slate-900/60 border border-slate-800/80 rounded-3xl space-y-6">
              <div>
                <div className="flex items-center gap-2 text-xs text-slate-400 mb-2">
                  <span className="text-emerald-400 font-medium">{item.categoryName}</span>
                  {item.subCategory && (
                    <>
                      <span>•</span>
                      <span>{item.subCategory}</span>
                    </>
                  )}
                  <span>•</span>
                  <div className="flex items-center gap-1">
                    <MapPin className="w-3.5 h-3.5 text-slate-400" />
                    <span>{item.location}</span>
                  </div>
                </div>
                <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                  {item.title}
                </h1>
              </div>

              {/* Badges strip */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 py-4 border-y border-slate-800 text-xs">
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80">
                  <div className="text-slate-500 mb-1">Condition</div>
                  <div className="font-semibold text-slate-200">{item.condition.replace('_', ' ')}</div>
                </div>
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80">
                  <div className="text-slate-500 mb-1">Min Borrow</div>
                  <div className="font-semibold text-slate-200">{item.minBorrowDays} Day(s)</div>
                </div>
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80">
                  <div className="text-slate-500 mb-1">Max Borrow</div>
                  <div className="font-semibold text-slate-200">{item.maxBorrowDays} Days</div>
                </div>
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80">
                  <div className="text-slate-500 mb-1">Est. Value</div>
                  <div className="font-semibold text-slate-200">₹{item.estimatedValue || 'N/A'}</div>
                </div>
              </div>

              {/* Description */}
              <div>
                <h3 className="text-base font-bold text-white mb-2">About this equipment</h3>
                <p className="text-sm text-slate-300 leading-relaxed whitespace-pre-line">
                  {item.description}
                </p>
              </div>

              {/* Borrowing Rules */}
              {item.borrowingRules && (
                <div className="p-4 bg-amber-950/20 border border-amber-500/20 rounded-2xl">
                  <div className="flex items-center gap-2 text-xs font-bold text-amber-300 uppercase tracking-wider mb-1.5">
                    <AlertTriangle className="w-4 h-4 text-amber-400" />
                    Owner's Borrowing Rules
                  </div>
                  <p className="text-xs text-amber-200/90 leading-relaxed whitespace-pre-line">
                    {item.borrowingRules}
                  </p>
                </div>
              )}
            </div>

            {/* Owner Profile Card */}
            <div className="p-6 sm:p-8 bg-slate-900/60 border border-slate-800/80 rounded-3xl space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-slate-950 font-black text-xl flex items-center justify-center shadow-lg shadow-emerald-500/20">
                    {item.ownerName.charAt(0)}
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="text-lg font-bold text-white">{item.ownerName}</h3>
                      <div className="flex items-center gap-1 text-[11px] text-emerald-400 bg-emerald-950/80 border border-emerald-500/30 px-2 py-0.5 rounded-full font-mono">
                        <ShieldCheck className="w-3.5 h-3.5" />
                        {item.ownerReputation} Rep
                      </div>
                    </div>
                    <div className="text-xs text-slate-400 mt-1">
                      Member since {new Date(item.ownerJoinedDate).getFullYear()} • {item.ownerCompletedLendings} successful lendings
                    </div>
                  </div>
                </div>

                {!isOwner && (
                  <button
                    onClick={handleStartChat}
                    className="flex items-center justify-center gap-2 px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold rounded-xl border border-slate-700 transition-colors"
                  >
                    <MessageSquare className="w-4 h-4 text-emerald-400" />
                    Ask Owner a Question
                  </button>
                )}
              </div>

              {/* Owner Reviews */}
              <div className="pt-4 border-t border-slate-800">
                <h4 className="text-sm font-bold text-white mb-3">Community Reviews ({ratings.length})</h4>
                {ratings.length > 0 ? (
                  <div className="space-y-3">
                    {ratings.map((r) => (
                      <div key={r.id} className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 text-xs">
                        <div className="flex items-center justify-between mb-1">
                          <span className="font-semibold text-slate-200">{r.fromUserName}</span>
                          <RatingStars rating={r.score} size="sm" showNumber />
                        </div>
                        {r.reviewComment && (
                          <p className="text-slate-400 text-[11px] mt-1 italic">"{r.reviewComment}"</p>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-slate-500">No reviews yet for this owner.</p>
                )}
              </div>
            </div>
          </div>

          {/* Right Col: Live Booking Card & Calendar */}
          <div className="space-y-6">
            <div className="sticky top-24 bg-slate-900/90 border border-slate-800 rounded-3xl p-6 shadow-2xl backdrop-blur-xl space-y-6">
              {/* Pricing Header */}
              <div className="flex items-baseline justify-between border-b border-slate-800 pb-4">
                <div>
                  {item.dailyRate && item.dailyRate > 0 ? (
                    <div>
                      <span className="text-3xl font-extrabold text-white">₹{item.dailyRate}</span>
                      <span className="text-slate-400 text-xs"> / day</span>
                    </div>
                  ) : (
                    <span className="text-2xl font-extrabold text-emerald-400">Free to Borrow</span>
                  )}
                </div>
                {item.depositAmount && item.depositAmount > 0 && (
                  <div className="text-right">
                    <span className="text-xs text-slate-400">Refundable Deposit</span>
                    <div className="text-sm font-bold text-slate-200 font-mono">₹{item.depositAmount}</div>
                  </div>
                )}
              </div>

              {/* Availability Calendar */}
              <div>
                <label className="text-xs font-bold text-slate-300 uppercase tracking-wider block mb-2">
                  Select Borrow Dates
                </label>
                <AvailabilityCalendar
                  itemId={itemId}
                  minDays={item.minBorrowDays}
                  maxDays={item.maxBorrowDays}
                  onSelectRange={handleDateRangeSelect}
                />
              </div>

              {/* Date preview & cost calculation */}
              {startDate && endDate && availabilityResult && (
                <div className="p-4 bg-slate-950/70 border border-slate-800 rounded-2xl space-y-3 text-xs">
                  <div className="flex items-center justify-between text-slate-300">
                    <span>Borrow Duration</span>
                    <span className="font-bold text-white">{availabilityResult.totalDays} Days</span>
                  </div>
                  <div className="flex items-center justify-between text-slate-300">
                    <span>Rental Cost (₹{item.dailyRate} × {availabilityResult.totalDays}d)</span>
                    <span className="font-bold text-white">₹{availabilityResult.estimatedRentalCost}</span>
                  </div>
                  <div className="flex items-center justify-between text-slate-300">
                    <span>Refundable Deposit Escrow</span>
                    <span className="font-bold text-emerald-400">₹{availabilityResult.depositRequired}</span>
                  </div>
                  <div className="pt-2 border-t border-slate-800 flex items-center justify-between text-sm font-bold text-white">
                    <span>Estimated Total</span>
                    <span className="text-emerald-400">
                      ₹{availabilityResult.estimatedRentalCost + availabilityResult.depositRequired}
                    </span>
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="space-y-3">
                {isOwner ? (
                  <Link
                    to={`/items/${item.id}/edit`}
                    className="w-full py-3.5 bg-slate-800 hover:bg-slate-700 text-white font-bold rounded-2xl text-center block transition-colors"
                  >
                    Edit Listing Details
                  </Link>
                ) : (
                  <button
                    onClick={() => {
                      if (!isAuthenticated) {
                        error('Please sign in to request to borrow.');
                        navigate('/login');
                        return;
                      }
                      if (!startDate || !endDate) {
                        error('Please pick available start and return dates on the calendar first.');
                        return;
                      }
                      setBorrowModalOpen(true);
                    }}
                    disabled={!startDate || !endDate || Boolean(availabilityResult && !availabilityResult.isAvailable)}
                    className="w-full py-4 bg-emerald-500 hover:bg-emerald-400 disabled:opacity-40 disabled:cursor-not-allowed text-slate-950 font-bold rounded-2xl shadow-xl shadow-emerald-500/25 transition-all text-sm flex items-center justify-center gap-2"
                  >
                    <CheckCircle2 className="w-5 h-5 stroke-[2.5]" />
                    Request to Borrow
                  </button>
                )}

                <button
                  onClick={handleFavoriteToggle}
                  className={`w-full py-3 rounded-2xl border text-xs font-semibold flex items-center justify-center gap-2 transition-colors ${
                    isFavorited
                      ? 'bg-rose-950/40 border-rose-500/40 text-rose-300'
                      : 'bg-slate-950/60 border-slate-800 hover:bg-slate-800 text-slate-300'
                  }`}
                >
                  <Heart className={`w-4 h-4 ${isFavorited ? 'fill-rose-400 text-rose-400' : ''}`} />
                  {isFavorited ? 'Saved in Watchlist' : 'Add to Watchlist'}
                </button>
              </div>

              {/* Trust Badge */}
              <div className="pt-2 text-[11px] text-slate-500 text-center flex items-center justify-center gap-2">
                <Lock className="w-3.5 h-3.5 text-emerald-400" />
                <span>6-digit OTP verification required at handover</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Borrow Request Modal */}
      {borrowModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-lg w-full p-6 sm:p-8 space-y-6 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div>
                <h3 className="text-xl font-bold text-white">Send Borrow Request</h3>
                <p className="text-xs text-slate-400 mt-0.5">{item.title}</p>
              </div>
              <button
                onClick={() => setBorrowModalOpen(false)}
                className="p-1.5 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmitBorrowRequest} className="space-y-4">
              <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl text-xs space-y-2">
                <div className="flex justify-between text-slate-400">
                  <span>Selected Dates:</span>
                  <span className="font-semibold text-white">{startDate} to {endDate}</span>
                </div>
                <div className="flex justify-between text-slate-400">
                  <span>Refundable Deposit:</span>
                  <span className="font-semibold text-emerald-400">₹{item.depositAmount || 0}</span>
                </div>
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Purpose / Project (Optional)
                </label>
                <input
                  type="text"
                  placeholder="e.g. Backyard deck staining, weekend photoshoot"
                  value={borrowPurpose}
                  onChange={(e) => setBorrowPurpose(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Message to Owner <span className="text-rose-400">*</span>
                </label>
                <textarea
                  rows={3}
                  required
                  placeholder="Introduce yourself, specify your intended pickup time, and share your experience handling this gear..."
                  value={borrowMessage}
                  onChange={(e) => setBorrowMessage(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setBorrowModalOpen(false)}
                  className="px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingBorrow}
                  className="px-6 py-2.5 bg-emerald-500 hover:bg-emerald-400 disabled:opacity-50 text-slate-950 text-xs font-bold rounded-xl shadow-lg shadow-emerald-500/20"
                >
                  {submittingBorrow ? 'Submitting...' : 'Confirm Request'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Report Modal */}
      {reportModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <Flag className="w-5 h-5 text-rose-400" />
                Report Inappropriate Content
              </h3>
              <button
                onClick={() => setReportModalOpen(false)}
                className="p-1 text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmitReport} className="space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-300 block mb-1">Reason</label>
                <select
                  value={reportReason}
                  onChange={(e) => setReportReason(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-emerald-500"
                >
                  <option value="MISLEADING_SPECIFICATIONS">Misleading specifications or photos</option>
                  <option value="PROHIBITED_OR_DANGEROUS">Prohibited / hazardous item</option>
                  <option value="SUSPECTED_FRAUD">Suspicious owner / potential fraud</option>
                  <option value="OFFENSIVE_CONTENT">Offensive or abusive language</option>
                </select>
              </div>

              <div>
                <label className="font-semibold text-slate-300 block mb-1">
                  Description / Evidence <span className="text-rose-400">*</span>
                </label>
                <textarea
                  rows={3}
                  required
                  placeholder="Explain why you are flagging this listing..."
                  value={reportDescription}
                  onChange={(e) => setReportDescription(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setReportModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 font-semibold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingReport}
                  className="px-5 py-2 bg-rose-600 hover:bg-rose-500 text-white font-bold rounded-xl"
                >
                  {submittingReport ? 'Submitting...' : 'Submit Report'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
