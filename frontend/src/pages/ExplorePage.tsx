import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Search,
  SlidersHorizontal,
  Box,
  MapPin,
  RefreshCw,
  X,
  Filter,
} from 'lucide-react';
import { itemService, ItemFilterParams } from '../services/itemService';
import { categoryService } from '../services/categoryService';
import { Category, ItemCondition, ItemSummary, LendingMode } from '../types';
import { ItemCard } from '../components/ItemCard';

export const ExplorePage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const [categories, setCategories] = useState<Category[]>([]);
  const [items, setItems] = useState<ItemSummary[]>([]);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [mobileFilterOpen, setMobileFilterOpen] = useState<boolean>(false);

  // Filters State
  const query = searchParams.get('query') || '';
  const categorySlug = searchParams.get('category') || '';
  const condition = (searchParams.get('condition') as ItemCondition) || '';
  const lendingMode = (searchParams.get('mode') as LendingMode) || '';
  const maxDailyRate = searchParams.get('maxPrice') ? Number(searchParams.get('maxPrice')) : undefined;
  const page = searchParams.get('page') ? Number(searchParams.get('page')) : 0;
  const sort = searchParams.get('sort') || 'createdAt,desc';

  useEffect(() => {
    categoryService.getAllCategories().then(setCategories).catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    const params: ItemFilterParams = {
      query: query || undefined,
      categorySlug: categorySlug || undefined,
      condition: condition || undefined,
      lendingMode: lendingMode || undefined,
      maxDailyRate: maxDailyRate || undefined,
      page,
      size: 12,
      sort,
    };

    itemService
      .searchItems(params)
      .then((res) => {
        setItems(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [query, categorySlug, condition, lendingMode, maxDailyRate, page, sort]);

  const updateParam = (key: string, value: string | null) => {
    const newParams = new URLSearchParams(searchParams);
    if (value) {
      newParams.set(key, value);
    } else {
      newParams.delete(key);
    }
    newParams.set('page', '0');
    setSearchParams(newParams);
  };

  const clearAllFilters = () => {
    setSearchParams(new URLSearchParams());
  };

  const hasActiveFilters = !!(query || categorySlug || condition || lendingMode || maxDailyRate);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Top Search & Filter Bar */}
        <div className="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight">Explore Gear</h1>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Found <span className="text-emerald-400 font-semibold">{totalElements}</span> items available to borrow
            </p>
          </div>

          <div className="flex items-center gap-3">
            {/* Search Input */}
            <div className="relative flex-1 md:w-80">
              <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                placeholder="Search items, tags..."
                value={query}
                onChange={(e) => updateParam('query', e.target.value || null)}
                className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
              />
              {query && (
                <button
                  onClick={() => updateParam('query', null)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              )}
            </div>

            {/* Sort Select */}
            <select
              value={sort}
              onChange={(e) => updateParam('sort', e.target.value)}
              className="bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-xs font-semibold text-slate-200 focus:outline-none focus:border-emerald-500"
            >
              <option value="createdAt,desc">Newest First</option>
              <option value="dailyRate,asc">Price: Low to High</option>
              <option value="dailyRate,desc">Price: High to Low</option>
              <option value="borrowCount,desc">Most Popular</option>
            </select>

            {/* Mobile filter toggle */}
            <button
              onClick={() => setMobileFilterOpen(!mobileFilterOpen)}
              className="md:hidden p-2 bg-slate-900 border border-slate-800 rounded-xl text-slate-300"
            >
              <Filter className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Layout Grid: Sidebar Filters + Items Grid */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Filters Sidebar */}
          <aside className={`md:block ${mobileFilterOpen ? 'block' : 'hidden'} space-y-6`}>
            <div className="p-5 bg-slate-900/70 border border-slate-800 rounded-2xl space-y-6">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <div className="flex items-center gap-2 text-sm font-bold text-white">
                  <SlidersHorizontal className="w-4 h-4 text-emerald-400" />
                  Filters
                </div>
                {hasActiveFilters && (
                  <button
                    onClick={clearAllFilters}
                    className="text-xs text-rose-400 hover:underline"
                  >
                    Reset all
                  </button>
                )}
              </div>

              {/* Categories */}
              <div>
                <label className="text-xs font-bold text-slate-300 uppercase tracking-wider block mb-2.5">
                  Category
                </label>
                <div className="space-y-1">
                  <button
                    onClick={() => updateParam('category', null)}
                    className={`w-full text-left px-3 py-1.5 rounded-lg text-xs transition-colors ${
                      !categorySlug
                        ? 'bg-emerald-500/20 text-emerald-300 font-semibold'
                        : 'text-slate-400 hover:bg-slate-800'
                    }`}
                  >
                    All Categories
                  </button>
                  {categories.map((c) => (
                    <button
                      key={c.id}
                      onClick={() => updateParam('category', c.slug)}
                      className={`w-full text-left px-3 py-1.5 rounded-lg text-xs transition-colors ${
                        categorySlug === c.slug
                          ? 'bg-emerald-500/20 text-emerald-300 font-semibold'
                          : 'text-slate-400 hover:bg-slate-800'
                      }`}
                    >
                      {c.name}
                    </button>
                  ))}
                </div>
              </div>

              {/* Condition */}
              <div>
                <label className="text-xs font-bold text-slate-300 uppercase tracking-wider block mb-2.5">
                  Condition
                </label>
                <div className="space-y-1 text-xs">
                  {['', 'NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'USED'].map((cond) => (
                    <button
                      key={cond || 'all'}
                      onClick={() => updateParam('condition', cond || null)}
                      className={`w-full text-left px-3 py-1.5 rounded-lg transition-colors ${
                        condition === cond
                          ? 'bg-emerald-500/20 text-emerald-300 font-semibold'
                          : 'text-slate-400 hover:bg-slate-800'
                      }`}
                    >
                      {cond ? cond.replace('_', ' ') : 'Any Condition'}
                    </button>
                  ))}
                </div>
              </div>

              {/* Lending Mode */}
              <div>
                <label className="text-xs font-bold text-slate-300 uppercase tracking-wider block mb-2.5">
                  Lending Mode
                </label>
                <div className="space-y-1 text-xs">
                  {[
                    { key: '', label: 'All Modes' },
                    { key: 'FREE', label: 'Free to Borrow' },
                    { key: 'DAILY_RATE', label: 'Daily Rate' },
                    { key: 'DEPOSIT_ONLY', label: 'Deposit Only' },
                    { key: 'RATE_AND_DEPOSIT', label: 'Rate & Deposit' },
                  ].map((m) => (
                    <button
                      key={m.key}
                      onClick={() => updateParam('mode', m.key || null)}
                      className={`w-full text-left px-3 py-1.5 rounded-lg transition-colors ${
                        lendingMode === m.key
                          ? 'bg-emerald-500/20 text-emerald-300 font-semibold'
                          : 'text-slate-400 hover:bg-slate-800'
                      }`}
                    >
                      {m.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </aside>

          {/* Items Grid */}
          <main className="md:col-span-3 space-y-6">
            {loading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {[1, 2, 3, 4, 5, 6].map((n) => (
                  <div
                    key={n}
                    className="h-80 bg-slate-900/40 border border-slate-800/60 rounded-2xl animate-pulse"
                  />
                ))}
              </div>
            ) : items.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {items.map((item) => (
                  <ItemCard key={item.id} item={item} />
                ))}
              </div>
            ) : (
              <div className="text-center py-20 bg-slate-900/30 border border-slate-800/80 rounded-2xl space-y-3">
                <Box className="w-12 h-12 text-slate-600 mx-auto" />
                <h3 className="text-lg font-bold text-white">No items match your criteria</h3>
                <p className="text-xs text-slate-400 max-w-sm mx-auto">
                  Try adjusting your search keywords, clear active category filters, or explore other options.
                </p>
                {hasActiveFilters && (
                  <button
                    onClick={clearAllFilters}
                    className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-emerald-400 text-xs font-semibold rounded-lg"
                  >
                    Clear All Filters
                  </button>
                )}
              </div>
            )}

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-2 pt-6">
                <button
                  disabled={page === 0}
                  onClick={() => updateParam('page', String(page - 1))}
                  className="px-3 py-1.5 bg-slate-900 border border-slate-800 rounded-lg text-xs font-semibold text-slate-300 disabled:opacity-30"
                >
                  Previous
                </button>
                <span className="text-xs text-slate-500 px-2">
                  Page <span className="text-white font-bold">{page + 1}</span> of {totalPages}
                </span>
                <button
                  disabled={page >= totalPages - 1}
                  onClick={() => updateParam('page', String(page + 1))}
                  className="px-3 py-1.5 bg-slate-900 border border-slate-800 rounded-lg text-xs font-semibold text-slate-300 disabled:opacity-30"
                >
                  Next
                </button>
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
};
