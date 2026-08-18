import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Heart, MapPin, ShieldCheck, Sparkles, Box } from 'lucide-react';
import { ItemSummary } from '../types';
import { RatingStars } from './RatingStars';
import { favoriteService } from '../services/favoriteService';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

interface ItemCardProps {
  item: ItemSummary;
  initialFavorited?: boolean;
  onFavoriteToggle?: (itemId: number, isFavorited: boolean) => void;
}

export const ItemCard: React.FC<ItemCardProps> = ({
  item,
  initialFavorited = false,
  onFavoriteToggle,
}) => {
  const { isAuthenticated } = useAuth();
  const { success, error } = useToast();
  const [isFavorited, setIsFavorited] = useState<boolean>(initialFavorited);
  const [favLoading, setFavLoading] = useState<boolean>(false);

  const handleFavoriteClick = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!isAuthenticated) {
      error('Please sign in to save items to your watchlist.');
      return;
    }

    setFavLoading(true);
    try {
      const res = await favoriteService.toggleFavorite(item.id);
      setIsFavorited(res.isFavorited);
      if (res.isFavorited) {
        success('Added to your watchlist!', item.title);
      } else {
        success('Removed from your watchlist.', item.title);
      }
      onFavoriteToggle?.(item.id, res.isFavorited);
    } catch {
      error('Failed to update watchlist.');
    } finally {
      setFavLoading(false);
    }
  };

  const conditionLabels = {
    NEW: { label: 'Brand New', color: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' },
    LIKE_NEW: { label: 'Like New', color: 'bg-teal-500/20 text-teal-300 border-teal-500/30' },
    GOOD: { label: 'Good', color: 'bg-blue-500/20 text-blue-300 border-blue-500/30' },
    FAIR: { label: 'Fair', color: 'bg-amber-500/20 text-amber-300 border-amber-500/30' },
    USED: { label: 'Well Used', color: 'bg-slate-700 text-slate-300 border-slate-600' },
  };

  return (
    <Link
      to={`/items/${item.id}`}
      className="group flex flex-col bg-slate-900/60 hover:bg-slate-900 border border-slate-800/80 hover:border-emerald-500/40 rounded-2xl overflow-hidden transition-all duration-300 hover:shadow-xl hover:shadow-emerald-950/20 hover:-translate-y-1"
    >
      {/* Thumbnail */}
      <div className="relative aspect-[4/3] w-full bg-slate-950 overflow-hidden">
        {item.primaryImageUrl ? (
          <img
            src={item.primaryImageUrl.startsWith('http') ? item.primaryImageUrl : `http://localhost:8080${item.primaryImageUrl}`}
            alt={item.title}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            onError={(e) => {
              // Fallback placeholder
              (e.target as HTMLElement).style.display = 'none';
            }}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-slate-950 to-slate-900 text-slate-700">
            <Box className="w-12 h-12" />
          </div>
        )}

        {/* Condition pill */}
        <div className="absolute top-3 left-3">
          <span
            className={`px-2.5 py-1 text-[11px] font-semibold tracking-wide uppercase rounded-full border backdrop-blur-md ${
              conditionLabels[item.condition]?.color || 'bg-slate-800 text-slate-300'
            }`}
          >
            {conditionLabels[item.condition]?.label || item.condition}
          </span>
        </div>

        {/* Favorite button */}
        <button
          onClick={handleFavoriteClick}
          disabled={favLoading}
          aria-label="Save to favorites"
          className={`absolute top-3 right-3 p-2 rounded-full backdrop-blur-md border transition-all ${
            isFavorited
              ? 'bg-rose-500/90 border-rose-400 text-white shadow-lg shadow-rose-500/30 scale-110'
              : 'bg-slate-950/60 hover:bg-slate-900 border-slate-700/60 text-slate-300 hover:text-rose-400'
          }`}
        >
          <Heart className={`w-4 h-4 ${isFavorited ? 'fill-white' : ''}`} />
        </button>

        {/* Lending mode badge */}
        {item.lendingMode === 'FREE' && (
          <div className="absolute bottom-3 left-3 px-2.5 py-0.5 bg-emerald-500 text-slate-950 text-xs font-bold rounded-md shadow-sm">
            FREE TO BORROW
          </div>
        )}
      </div>

      {/* Content */}
      <div className="flex-1 p-4 flex flex-col justify-between gap-3">
        <div>
          {/* Category & Location */}
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1.5">
            <span className="text-emerald-400/90 font-medium truncate max-w-[60%]">
              {item.categoryName || 'General Gear'}
            </span>
            <div className="flex items-center gap-1 text-[11px] text-slate-500 truncate">
              <MapPin className="w-3 h-3 shrink-0" />
              <span className="truncate">{item.location}</span>
            </div>
          </div>

          {/* Title */}
          <h3 className="font-semibold text-base text-slate-100 group-hover:text-emerald-300 line-clamp-1 transition-colors">
            {item.title}
          </h3>
        </div>

        {/* Owner reputation & rating */}
        <div className="flex items-center justify-between pt-2 border-t border-slate-800/80 text-xs">
          <div className="flex items-center gap-1.5">
            <div className="w-5 h-5 rounded-full bg-slate-800 text-slate-300 font-bold text-[10px] flex items-center justify-center">
              {item.ownerName?.charAt(0) || 'O'}
            </div>
            <span className="text-slate-300 font-medium truncate max-w-[90px]">
              {item.ownerName}
            </span>
            <div className="flex items-center gap-0.5 text-[10px] text-emerald-400 bg-emerald-950/60 border border-emerald-500/20 px-1 py-0.2 rounded font-mono">
              <ShieldCheck className="w-2.5 h-2.5" />
              {item.ownerReputation || 80}
            </div>
          </div>

          <RatingStars rating={item.ownerRating || 5.0} size="sm" showNumber />
        </div>

        {/* Pricing footer */}
        <div className="flex items-center justify-between pt-2 text-xs">
          <div>
            {item.dailyRate && item.dailyRate > 0 ? (
              <div>
                <span className="text-base font-bold text-white">₹{item.dailyRate}</span>
                <span className="text-slate-500 text-[11px]"> / day</span>
              </div>
            ) : (
              <span className="text-sm font-bold text-emerald-400">Community Free</span>
            )}
          </div>

          {item.depositAmount && item.depositAmount > 0 ? (
            <div className="text-[11px] text-slate-400 bg-slate-800/80 px-2 py-0.5 rounded-md border border-slate-700/50">
              ₹{item.depositAmount} deposit
            </div>
          ) : (
            <div className="text-[11px] text-emerald-400/80">No deposit</div>
          )}
        </div>
      </div>
    </Link>
  );
};
