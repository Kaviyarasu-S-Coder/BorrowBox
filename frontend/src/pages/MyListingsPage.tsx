import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Box, PlusCircle, Edit3, Eye, ArrowRightLeft, Sparkles, MapPin } from 'lucide-react';
import { itemService } from '../services/itemService';
import { ItemSummary } from '../types';

export const MyListingsPage: React.FC = () => {
  const [items, setItems] = useState<ItemSummary[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    itemService
      .getMyItems(0, 20)
      .then((res) => setItems(res.content))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight">My Listed Gear</h1>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Manage your equipment inventory, adjust rental rates, and track borrow counts.
            </p>
          </div>
          <Link
            to="/items/create"
            className="flex items-center gap-2 px-5 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold rounded-xl shadow-lg shadow-emerald-500/20"
          >
            <PlusCircle className="w-4 h-4 stroke-[2.5]" />
            List New Gear
          </Link>
        </div>

        {loading ? (
          <div className="text-center py-20 text-slate-500">Loading your listings...</div>
        ) : items.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {items.map((item) => (
              <div
                key={item.id}
                className="bg-slate-900 border border-slate-800 rounded-3xl overflow-hidden flex flex-col justify-between"
              >
                <div className="relative aspect-[16/10] bg-slate-950">
                  {item.primaryImageUrl ? (
                    <img
                      src={item.primaryImageUrl.startsWith('http') ? item.primaryImageUrl : `http://localhost:8080${item.primaryImageUrl}`}
                      alt={item.title}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-slate-700">
                      <Box className="w-12 h-12" />
                    </div>
                  )}
                  <div className="absolute top-3 left-3 px-2.5 py-0.5 bg-slate-950/80 backdrop-blur-md border border-slate-700 text-emerald-400 text-[10px] font-bold rounded-full uppercase">
                    {item.status}
                  </div>
                </div>

                <div className="p-5 space-y-4 flex-1 flex flex-col justify-between">
                  <div>
                    <div className="text-xs text-emerald-400 font-medium mb-1">
                      {item.categoryName}
                    </div>
                    <h3 className="font-bold text-white text-base line-clamp-1">{item.title}</h3>
                    <div className="flex items-center gap-4 text-xs text-slate-400 mt-3 pt-3 border-t border-slate-800">
                      <span>Rate: <strong className="text-white">₹{item.dailyRate || 0}/d</strong></span>
                      <span>Deposit: <strong className="text-white">₹{item.depositAmount || 0}</strong></span>
                      <span>Borrows: <strong className="text-emerald-400">{item.borrowCount}</strong></span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 pt-2">
                    <Link
                      to={`/items/${item.id}`}
                      className="flex-1 py-2 text-center bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl transition-colors"
                    >
                      View Live
                    </Link>
                    <Link
                      to={`/items/${item.id}/edit`}
                      className="flex-1 py-2 text-center bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 text-xs font-semibold rounded-xl border border-emerald-500/30 transition-colors flex items-center justify-center gap-1"
                    >
                      <Edit3 className="w-3.5 h-3.5" />
                      Edit
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-20 bg-slate-900/40 border border-slate-800 rounded-3xl space-y-3">
            <Box className="w-12 h-12 text-slate-600 mx-auto" />
            <h3 className="text-lg font-bold text-white">No items listed yet</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              Start sharing equipment and earn extra money while helping your local community.
            </p>
            <Link
              to="/items/create"
              className="inline-block px-5 py-2.5 bg-emerald-500 text-slate-950 text-xs font-bold rounded-xl"
            >
              List Your First Item
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};
