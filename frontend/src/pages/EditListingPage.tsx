import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, ArrowRight } from 'lucide-react';
import { itemService, UpdateItemPayload } from '../services/itemService';
import { categoryService } from '../services/categoryService';
import { Category, ItemCondition, ItemDetail, ItemStatus, LendingMode } from '../types';
import { useToast } from '../context/ToastContext';

export const EditListingPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { success, error } = useToast();

  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);

  // Form
  const [title, setTitle] = useState<string>('');
  const [categoryId, setCategoryId] = useState<number>(1);
  const [subCategory, setSubCategory] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [condition, setCondition] = useState<ItemCondition>('GOOD');
  const [status, setStatus] = useState<ItemStatus>('AVAILABLE');
  const [lendingMode, setLendingMode] = useState<LendingMode>('DAILY_RATE');
  const [dailyRate, setDailyRate] = useState<number | ''>('');
  const [depositAmount, setDepositAmount] = useState<number | ''>('');
  const [estimatedValue, setEstimatedValue] = useState<number | ''>('');
  const [minBorrowDays, setMinBorrowDays] = useState<number>(1);
  const [maxBorrowDays, setMaxBorrowDays] = useState<number>(14);
  const [borrowingRules, setBorrowingRules] = useState<string>('');
  const [location, setLocation] = useState<string>('');

  const itemId = Number(id);

  useEffect(() => {
    Promise.all([
      categoryService.getAllCategories().catch(() => []),
      itemService.getItemById(itemId),
    ])
      .then(([cats, item]) => {
        setCategories(cats);
        setTitle(item.title);
        if (item.categoryId) setCategoryId(item.categoryId);
        setSubCategory(item.subCategory || '');
        setDescription(item.description);
        setCondition(item.condition);
        setStatus(item.status);
        setLendingMode(item.lendingMode);
        setDailyRate(item.dailyRate || 0);
        setDepositAmount(item.depositAmount || 0);
        setEstimatedValue(item.estimatedValue || 0);
        setMinBorrowDays(item.minBorrowDays);
        setMaxBorrowDays(item.maxBorrowDays);
        setBorrowingRules(item.borrowingRules || '');
        setLocation(item.location);
      })
      .catch(() => {
        error('Item not found.');
        navigate('/my-items');
      })
      .finally(() => setLoading(false));
  }, [itemId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload: UpdateItemPayload = {
        categoryId,
        subCategory: subCategory.trim() || undefined,
        title: title.trim(),
        description: description.trim(),
        condition,
        status,
        lendingMode,
        dailyRate: Number(dailyRate || 0),
        depositAmount: Number(depositAmount || 0),
        estimatedValue: Number(estimatedValue || 0),
        minBorrowDays: Number(minBorrowDays || 1),
        maxBorrowDays: Number(maxBorrowDays || 14),
        borrowingRules: borrowingRules.trim() || undefined,
        location: location.trim(),
      };

      await itemService.updateItem(itemId, payload);
      success('Listing updated successfully!');
      navigate(`/items/${itemId}`);
    } catch (err: any) {
      error(err.response?.data?.message || 'Failed to update item.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center text-slate-500">
        Loading listing details...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="text-3xl font-extrabold text-white tracking-tight mb-8">
          Edit Item Listing
        </h1>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="p-6 bg-slate-900/70 border border-slate-800 rounded-3xl space-y-4">
            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1.5">Title</label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="w-full px-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">Category</label>
                <select
                  value={categoryId}
                  onChange={(e) => setCategoryId(Number(e.target.value))}
                  className="w-full px-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white"
                >
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">Status</label>
                <select
                  value={status}
                  onChange={(e) => setStatus(e.target.value as ItemStatus)}
                  className="w-full px-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white"
                >
                  <option value="AVAILABLE">Available</option>
                  <option value="MAINTENANCE">Maintenance</option>
                  <option value="INACTIVE">Inactive / Hidden</option>
                </select>
              </div>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1.5">Description</label>
              <textarea
                rows={4}
                required
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">Daily Rate (₹)</label>
                <input
                  type="number"
                  min="0"
                  value={dailyRate}
                  onChange={(e) => setDailyRate(Number(e.target.value))}
                  className="w-full px-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white"
                />
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300 block mb-1.5">Deposit Amount (₹)</label>
                <input
                  type="number"
                  min="0"
                  value={depositAmount}
                  onChange={(e) => setDepositAmount(Number(e.target.value))}
                  className="w-full px-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white"
                />
              </div>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1.5">Borrowing Rules</label>
              <textarea
                rows={2}
                value={borrowingRules}
                onChange={(e) => setBorrowingRules(e.target.value)}
                className="w-full px-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white"
              />
            </div>
          </div>

          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="px-6 py-2.5 bg-slate-800 text-slate-300 text-xs font-semibold rounded-xl"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-6 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold rounded-xl shadow-lg"
            >
              {saving ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
