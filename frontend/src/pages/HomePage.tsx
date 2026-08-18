import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Search,
  Box,
  ShieldCheck,
  Zap,
  Lock,
  ArrowRight,
  Sparkles,
  Camera,
  Wrench,
  Tent,
  Music,
  Scissors,
  Laptop,
  CheckCircle2,
  Users,
  Award,
} from 'lucide-react';
import { itemService } from '../services/itemService';
import { categoryService } from '../services/categoryService';
import { Category, ItemSummary } from '../types';
import { ItemCard } from '../components/ItemCard';

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [categories, setCategories] = useState<Category[]>([]);
  const [recentItems, setRecentItems] = useState<ItemSummary[]>([]);
  const [popularItems, setPopularItems] = useState<ItemSummary[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    Promise.all([
      categoryService.getAllCategories().catch(() => []),
      itemService.getRecentlyListed().catch(() => []),
      itemService.getPopularItems().catch(() => []),
    ]).then(([cats, recents, populars]) => {
      setCategories(cats);
      setRecentItems(recents);
      setPopularItems(populars);
      setLoading(false);
    });
  }, []);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/explore?query=${encodeURIComponent(searchQuery.trim())}`);
    } else {
      navigate('/explore');
    }
  };

  const categoryIcons: Record<string, React.ReactNode> = {
    'power-tools': <Wrench className="w-6 h-6 text-amber-400" />,
    'cameras-photography': <Camera className="w-6 h-6 text-cyan-400" />,
    'camping-outdoors': <Tent className="w-6 h-6 text-emerald-400" />,
    'musical-instruments': <Music className="w-6 h-6 text-purple-400" />,
    'lawn-garden': <Scissors className="w-6 h-6 text-lime-400" />,
    'electronics': <Laptop className="w-6 h-6 text-blue-400" />,
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 lg:pt-20 lg:pb-28 border-b border-slate-900 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(16,185,129,0.15),rgba(255,255,255,0))]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
          {/* Tagline Pill */}
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-emerald-950/60 border border-emerald-500/30 text-emerald-300 text-xs font-semibold uppercase tracking-wider mb-6 animate-fade-in shadow-lg shadow-emerald-950/40">
            <Sparkles className="w-3.5 h-3.5 text-emerald-400" />
            Borrow what you need. Share what you have.
          </div>

          {/* Main Headline */}
          <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight text-white max-w-4xl mx-auto leading-tight sm:leading-none mb-6">
            Why buy when you can{' '}
            <span className="bg-gradient-to-r from-emerald-400 via-teal-300 to-cyan-400 bg-clip-text text-transparent">
              Borrow from Neighbors?
            </span>
          </h1>

          <p className="text-base sm:text-xl text-slate-400 max-w-2xl mx-auto mb-10 leading-relaxed">
            Rent high-end power tools, camera equipment, camping gear, and party supplies locally.
            Verified handovers with 6-digit OTP security and refundable deposit escrow.
          </p>

          {/* Search Box */}
          <form
            onSubmit={handleSearchSubmit}
            className="max-w-2xl mx-auto flex items-center bg-slate-900/90 border border-slate-700/80 hover:border-emerald-500/50 rounded-2xl p-2 shadow-2xl backdrop-blur-xl transition-all focus-within:ring-2 focus-within:ring-emerald-500/40 focus-within:border-emerald-500"
          >
            <div className="pl-3 text-slate-400">
              <Search className="w-5 h-5" />
            </div>
            <input
              type="text"
              placeholder="What do you need? (e.g. Sony A7 III, Pressure Washer, Camping Tent)"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="flex-1 bg-transparent border-0 text-white placeholder-slate-500 text-sm sm:text-base px-3 py-2 focus:outline-none"
            />
            <button
              type="submit"
              className="px-6 py-3 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-sm font-bold rounded-xl shadow-md shadow-emerald-500/25 transition-all shrink-0"
            >
              Search Gear
            </button>
          </form>

          {/* Popular search tags */}
          <div className="flex flex-wrap items-center justify-center gap-2 mt-6 text-xs text-slate-400">
            <span className="text-slate-500 font-medium">Popular:</span>
            {['Drill', 'Camera', 'Projector', 'Tent', 'Lawn Mower', 'Tripod'].map((tag) => (
              <button
                key={tag}
                onClick={() => navigate(`/explore?query=${tag}`)}
                className="px-3 py-1 bg-slate-900/60 hover:bg-slate-800 border border-slate-800 hover:border-slate-700 rounded-lg text-slate-300 transition-colors"
              >
                {tag}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* Categories Grid */}
      <section className="py-16 bg-slate-950 border-b border-slate-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h2 className="text-2xl font-bold text-white tracking-tight">Explore Categories</h2>
              <p className="text-xs sm:text-sm text-slate-400 mt-1">Browse verified equipment in your neighborhood</p>
            </div>
            <Link
              to="/explore"
              className="text-xs sm:text-sm font-semibold text-emerald-400 hover:text-emerald-300 flex items-center gap-1"
            >
              View all <ArrowRight className="w-4 h-4" />
            </Link>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
            {categories.map((cat) => (
              <Link
                key={cat.id}
                to={`/explore?category=${cat.slug}`}
                className="group p-5 bg-slate-900/50 hover:bg-slate-900 border border-slate-800/80 hover:border-emerald-500/40 rounded-2xl text-center flex flex-col items-center justify-center gap-3 transition-all hover:-translate-y-1 hover:shadow-lg hover:shadow-emerald-950/20"
              >
                <div className="w-12 h-12 rounded-xl bg-slate-800 group-hover:bg-emerald-950/60 border border-slate-700/60 group-hover:border-emerald-500/30 flex items-center justify-center transition-colors">
                  {categoryIcons[cat.slug] || <Box className="w-6 h-6 text-emerald-400" />}
                </div>
                <div>
                  <div className="font-semibold text-sm text-slate-200 group-hover:text-emerald-300 transition-colors">
                    {cat.name}
                  </div>
                  <div className="text-[11px] text-slate-500 mt-0.5">
                    {cat.subCategories?.length ? `${cat.subCategories.length} subcategories` : 'Verified gear'}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Featured / Popular Items Grid */}
      <section className="py-16 bg-slate-950/60 border-b border-slate-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <div>
              <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-emerald-400 uppercase tracking-wider mb-1">
                <Zap className="w-3.5 h-3.5" /> High Demand
              </div>
              <h2 className="text-2xl font-bold text-white tracking-tight">Most Popular Items</h2>
            </div>
            <Link
              to="/explore?sort=popularity"
              className="text-xs sm:text-sm font-semibold text-emerald-400 hover:text-emerald-300 flex items-center gap-1"
            >
              See more <ArrowRight className="w-4 h-4" />
            </Link>
          </div>

          {popularItems.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {popularItems.slice(0, 4).map((item) => (
                <ItemCard key={item.id} item={item} />
              ))}
            </div>
          ) : (
            <div className="text-center py-12 text-slate-500 text-sm">
              Explore available items and be the first to borrow!
            </div>
          )}
        </div>
      </section>

      {/* Recently Listed Items */}
      <section className="py-16 bg-slate-950 border-b border-slate-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <div>
              <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-cyan-400 uppercase tracking-wider mb-1">
                <Sparkles className="w-3.5 h-3.5" /> Fresh Listings
              </div>
              <h2 className="text-2xl font-bold text-white tracking-tight">Recently Added Gear</h2>
            </div>
            <Link
              to="/explore?sort=newest"
              className="text-xs sm:text-sm font-semibold text-emerald-400 hover:text-emerald-300 flex items-center gap-1"
            >
              Browse all <ArrowRight className="w-4 h-4" />
            </Link>
          </div>

          {recentItems.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {recentItems.slice(0, 4).map((item) => (
                <ItemCard key={item.id} item={item} />
              ))}
            </div>
          ) : (
            <div className="text-center py-12 text-slate-500 text-sm">
              No recent items found.
            </div>
          )}
        </div>
      </section>

      {/* How BorrowBox Works */}
      <section className="py-20 bg-gradient-to-b from-slate-950 to-slate-900/60 border-b border-slate-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight mb-4">
              How BorrowBox Works
            </h2>
            <p className="text-slate-400 text-sm sm:text-base">
              Safe, transparent, and verified sharing with zero hidden fees.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            {[
              {
                step: '01',
                title: 'Find & Request',
                desc: 'Search gear in your local area and select available borrow dates on the live calendar.',
                icon: <Search className="w-6 h-6 text-emerald-400" />,
              },
              {
                step: '02',
                title: 'Owner Approval',
                desc: 'Owner reviews your borrower reputation score and accepts the booking terms.',
                icon: <CheckCircle2 className="w-6 h-6 text-teal-400" />,
              },
              {
                step: '03',
                title: 'OTP Handover',
                desc: 'Meet in person, inspect item condition, and exchange 6-digit OTP code to start borrow.',
                icon: <ShieldCheck className="w-6 h-6 text-cyan-400" />,
              },
              {
                step: '04',
                title: 'Return & Review',
                desc: 'Return the item on time, verify return OTP, release deposit escrow, and leave 4D reviews.',
                icon: <Award className="w-6 h-6 text-amber-400" />,
              },
            ].map((st) => (
              <div
                key={st.step}
                className="relative p-6 bg-slate-900 border border-slate-800 rounded-2xl flex flex-col justify-between hover:border-slate-700 transition-colors"
              >
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <div className="w-12 h-12 rounded-xl bg-slate-800/80 flex items-center justify-center">
                      {st.icon}
                    </div>
                    <span className="font-mono text-2xl font-black text-slate-700">
                      {st.step}
                    </span>
                  </div>
                  <h3 className="text-lg font-bold text-white mb-2">{st.title}</h3>
                  <p className="text-xs text-slate-400 leading-relaxed">{st.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Trust & Safety Banner */}
      <section className="py-16 bg-slate-950">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="p-8 sm:p-12 rounded-3xl bg-gradient-to-r from-emerald-950/80 via-slate-900 to-teal-950/80 border border-emerald-500/30 flex flex-col md:flex-row items-center justify-between gap-8 shadow-2xl">
            <div className="max-w-xl space-y-4">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/20 text-emerald-300 text-xs font-bold uppercase">
                <Lock className="w-3.5 h-3.5" /> 100% Protected Platform
              </div>
              <h3 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                Have idle tools or camera gear? Start earning safely today.
              </h3>
              <p className="text-sm text-slate-300 leading-relaxed">
                List in under 2 minutes. You control daily rates, security deposit amount, and minimum borrow periods.
              </p>
            </div>
            <Link
              to="/items/create"
              className="px-8 py-4 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded-2xl shadow-xl shadow-emerald-500/25 shrink-0 text-center transition-all hover:scale-105"
            >
              List Your Gear Free
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
};
