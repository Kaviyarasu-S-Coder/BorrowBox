import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  PlusCircle,
  Image as ImageIcon,
  MapPin,
  Coins,
  ShieldAlert,
  ArrowRight,
  Sparkles,
} from 'lucide-react';
import { itemService, CreateItemPayload } from '../services/itemService';
import { categoryService } from '../services/categoryService';
import { Category, ItemCondition, LendingMode } from '../types';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export const CreateListingPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const { success, error } = useToast();

  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  // Form State
  const [title, setTitle] = useState<string>('');
  const [categoryId, setCategoryId] = useState<number | ''>('');
  const [subCategory, setSubCategory] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [condition, setCondition] = useState<ItemCondition>('GOOD');
  const [lendingMode, setLendingMode] = useState<LendingMode>('DAILY_RATE');
  const [dailyRate, setDailyRate] = useState<number | ''>(250);
  const [depositAmount, setDepositAmount] = useState<number | ''>(1500);
  const [estimatedValue, setEstimatedValue] = useState<number | ''>(8000);
  const [minBorrowDays, setMinBorrowDays] = useState<number>(1);
  const [maxBorrowDays, setMaxBorrowDays] = useState<number>(7);
  const [borrowingRules, setBorrowingRules] = useState<string>('');
  const [location, setLocation] = useState<string>(user?.location || 'Indiranagar, Bangalore');
  const [imageUrls, setImageUrls] = useState<string[]>(['https://images.unsplash.com/photo-1504148455328-c376907d081c?w=800']);

  useEffect(() => {
    categoryService.getAllCategories().then((cats) => {
      setCategories(cats);
      if (cats.length > 0) setCategoryId(cats[0].id);
    }).catch(() => {});
  }, []);

  const handleAddImageUrl = () => {
    setImageUrls([...imageUrls, '']);
  };

  const handleImageUrlChange = (index: number, val: string) => {
    const updated = [...imageUrls];
    updated[index] = val;
    setImageUrls(updated);
  };

  const handleRemoveImageUrl = (index: number) => {
    setImageUrls(imageUrls.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!categoryId) {
      error('Please select a category.');
      return;
    }
    if (!title.trim() || !description.trim()) {
      error('Title and description are required.');
      return;
    }

    const filteredImages = imageUrls.filter((url) => url.trim().length > 0);

    const payload: CreateItemPayload = {
      categoryId: Number(categoryId),
      subCategory: subCategory.trim() || undefined,
      title: title.trim(),
      description: description.trim(),
      condition,
      lendingMode,
      dailyRate: lendingMode === 'FREE' ? 0 : Number(dailyRate || 0),
      depositAmount: Number(depositAmount || 0),
      estimatedValue: Number(estimatedValue || 0),
      minBorrowDays: Number(minBorrowDays || 1),
      maxBorrowDays: Number(maxBorrowDays || 14),
      borrowingRules: borrowingRules.trim() || undefined,
      location: location.trim(),
      imageUrls: filteredImages,
    };

    setLoading(true);
    try {
      const created = await itemService.createItem(payload);
      success('Item listed successfully! Neighbors can now discover and request to borrow it.');
      navigate(`/items/${created.id}`);
    } catch (err: any) {
      error(err.response?.data?.message || 'Failed to create item listing.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-emerald-400 uppercase tracking-wider mb-1">
            <Sparkles className="w-3.5 h-3.5" /> Peer-to-Peer Sharing
          </div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">
            List Equipment or Tools for Borrowing
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Share your idle items safely with neighbors. You set daily rates, deposit escrow, and borrowing rules.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-8">
          {/* Card 1: Core Info */}
          <div className="p-6 sm:p-8 bg-slate-900/70 border border-slate-800 rounded-3xl space-y-6">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <Box className="w-5 h-5 text-emerald-400" />
              Item Information
            </h2>

            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                Item Title <span className="text-rose-400">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g. DeWalt 20V Max Cordless Drill Kit with 2 Batteries"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Category <span className="text-rose-400">*</span>
                </label>
                <select
                  required
                  value={categoryId}
                  onChange={(e) => setCategoryId(Number(e.target.value))}
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                >
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Subcategory (Optional)
                </label>
                <input
                  type="text"
                  placeholder="e.g. Power Drills, Prime Lenses"
                  value={subCategory}
                  onChange={(e) => setSubCategory(e.target.value)}
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                Detailed Description <span className="text-rose-400">*</span>
              </label>
              <textarea
                rows={4}
                required
                placeholder="Describe condition, what accessories are included (chargers, case, blades), and any handling tips..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Item Condition
                </label>
                <select
                  value={condition}
                  onChange={(e) => setCondition(e.target.value as ItemCondition)}
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                >
                  <option value="NEW">Brand New</option>
                  <option value="LIKE_NEW">Like New</option>
                  <option value="GOOD">Good</option>
                  <option value="FAIR">Fair</option>
                  <option value="USED">Well Used</option>
                </select>
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Handover Location
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Indiranagar, Bangalore"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>
            </div>
          </div>

          {/* Card 2: Pricing & Deposit */}
          <div className="p-6 sm:p-8 bg-slate-900/70 border border-slate-800 rounded-3xl space-y-6">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <Coins className="w-5 h-5 text-emerald-400" />
              Pricing & Security Deposit
            </h2>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Lending Mode
                </label>
                <select
                  value={lendingMode}
                  onChange={(e) => setLendingMode(e.target.value as LendingMode)}
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                >
                  <option value="DAILY_RATE">Daily Rental Rate</option>
                  <option value="FREE">Free to Borrow (Community)</option>
                  <option value="DEPOSIT_ONLY">Deposit Escrow Only (Free)</option>
                  <option value="RATE_AND_DEPOSIT">Rate & Deposit Combined</option>
                </select>
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Daily Rental Rate (₹ / Day)
                </label>
                <input
                  type="number"
                  min="0"
                  disabled={lendingMode === 'FREE' || lendingMode === 'DEPOSIT_ONLY'}
                  value={dailyRate}
                  onChange={(e) => setDailyRate(e.target.value ? Number(e.target.value) : '')}
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500 disabled:opacity-30"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Refundable Security Deposit (₹)
                </label>
                <input
                  type="number"
                  min="0"
                  value={depositAmount}
                  onChange={(e) => setDepositAmount(e.target.value ? Number(e.target.value) : '')}
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
                <span className="text-[11px] text-slate-500 mt-1 block">
                  Held securely in escrow and returned when return OTP is verified.
                </span>
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Estimated Item Replacement Value (₹)
                </label>
                <input
                  type="number"
                  min="0"
                  value={estimatedValue}
                  onChange={(e) => setEstimatedValue(e.target.value ? Number(e.target.value) : '')}
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Minimum Borrow Duration (Days)
                </label>
                <input
                  type="number"
                  min="1"
                  max="30"
                  value={minBorrowDays}
                  onChange={(e) => setMinBorrowDays(Number(e.target.value))}
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                  Maximum Borrow Duration (Days)
                </label>
                <input
                  type="number"
                  min="1"
                  max="90"
                  value={maxBorrowDays}
                  onChange={(e) => setMaxBorrowDays(Number(e.target.value))}
                  className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                />
              </div>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                Borrowing Rules & Care Instructions (Optional)
              </label>
              <textarea
                rows={2}
                placeholder="e.g. Please clean tool after use. Do not expose camera to rain. Charge batteries before return."
                value={borrowingRules}
                onChange={(e) => setBorrowingRules(e.target.value)}
                className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>

          {/* Card 3: Photos */}
          <div className="p-6 sm:p-8 bg-slate-900/70 border border-slate-800 rounded-3xl space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold text-white flex items-center gap-2">
                <ImageIcon className="w-5 h-5 text-emerald-400" />
                Photos
              </h2>
              <button
                type="button"
                onClick={handleAddImageUrl}
                className="text-xs text-emerald-400 font-semibold hover:underline"
              >
                + Add Another Image URL
              </button>
            </div>

            <div className="space-y-3">
              {imageUrls.map((url, idx) => (
                <div key={idx} className="flex items-center gap-2">
                  <input
                    type="url"
                    placeholder="https://example.com/image.jpg"
                    value={url}
                    onChange={(e) => handleImageUrlChange(idx, e.target.value)}
                    className="flex-1 px-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
                  />
                  {imageUrls.length > 1 && (
                    <button
                      type="button"
                      onClick={() => handleRemoveImageUrl(idx)}
                      className="px-3 py-2 bg-slate-800 hover:bg-rose-950 hover:text-rose-400 text-slate-400 text-xs rounded-xl transition-colors"
                    >
                      Remove
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Submit */}
          <div className="flex items-center justify-end gap-4">
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="px-6 py-3 bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold rounded-xl text-sm transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-8 py-3.5 bg-emerald-500 hover:bg-emerald-400 disabled:opacity-50 text-slate-950 font-bold rounded-xl shadow-xl shadow-emerald-500/25 transition-all text-sm flex items-center gap-2"
            >
              {loading ? 'Publishing...' : 'Publish Listing'}
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
