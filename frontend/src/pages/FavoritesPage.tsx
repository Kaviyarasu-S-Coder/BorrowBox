import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Heart, Box } from 'lucide-react';
import { favoriteService } from '../services/favoriteService';
import { ItemSummary } from '../types';
import { ItemCard } from '../components/ItemCard';

export const FavoritesPage: React.FC = () => {
  const [favorites, setFavorites] = useState<ItemSummary[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    favoriteService
      .getMyFavorites(0, 20)
      .then((res) => {
        setFavorites(res.content);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleFavoriteToggle = (itemId: number, isFavorited: boolean) => {
    if (!isFavorited) {
      setFavorites((prev) => prev.filter((item) => item.id !== itemId));
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-rose-400 uppercase tracking-wider mb-1">
            <Heart className="w-3.5 h-3.5 fill-rose-400" /> Watchlist
          </div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Saved Equipment</h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Items you have bookmarked for upcoming projects.
          </p>
        </div>

        {loading ? (
          <div className="text-center py-20 text-slate-500">Loading your saved items...</div>
        ) : favorites.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {favorites.map((item) => (
              <ItemCard
                key={item.id}
                item={item}
                initialFavorited={true}
                onFavoriteToggle={handleFavoriteToggle}
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-20 bg-slate-900/40 border border-slate-800 rounded-3xl space-y-3">
            <Heart className="w-12 h-12 text-slate-600 mx-auto" />
            <h3 className="text-lg font-bold text-white">Your watchlist is empty</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              Click the heart icon on any equipment card to save it for quick booking later.
            </p>
            <Link
              to="/explore"
              className="inline-block px-5 py-2.5 bg-emerald-500 text-slate-950 text-xs font-bold rounded-xl"
            >
              Explore Items
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};
