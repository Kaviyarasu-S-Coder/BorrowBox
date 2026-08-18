import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Users,
  Box,
  ArrowRightLeft,
  AlertTriangle,
  Coins,
  ShieldCheck,
  Zap,
  Layers,
  Flag,
  Activity,
} from 'lucide-react';
import { adminService } from '../../services/adminService';
import { AdminStats } from '../../types';

export const AdminDashboardPage: React.FC = () => {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    adminService
      .getDashboardStats()
      .then(setStats)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading || !stats) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center text-slate-500">
        Loading platform analytics...
      </div>
    );
  }

  const statCards = [
    { label: 'Total Registered Users', value: stats.totalUsers, icon: <Users className="w-5 h-5 text-blue-400" /> },
    { label: 'Available Items', value: stats.availableItems || stats.totalItems, icon: <Box className="w-5 h-5 text-emerald-400" /> },
    { label: 'Total Transactions', value: stats.totalTransactions, icon: <ArrowRightLeft className="w-5 h-5 text-cyan-400" /> },
    { label: 'Completed Lendings', value: stats.completedTransactions, icon: <ShieldCheck className="w-5 h-5 text-teal-400" /> },
    { label: 'Open Disputes', value: stats.openDisputes, icon: <AlertTriangle className="w-5 h-5 text-rose-400" /> },
    { label: 'Open Reports', value: stats.openReports, icon: <Flag className="w-5 h-5 text-amber-400" /> },
    { label: 'Active Borrowings', value: stats.activeTransactions, icon: <Activity className="w-5 h-5 text-purple-400" /> },
    { label: 'Deposit Escrow Held (₹)', value: `₹${stats.totalDepositHeld || 0}`, icon: <Coins className="w-5 h-5 text-emerald-400" /> },
  ];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-1.5 text-xs font-bold text-amber-400 uppercase tracking-wider mb-1">
              <ShieldCheck className="w-4 h-4" /> Platform Administration
            </div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight">Admin Operations & KPI Dashboard</h1>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to="/admin/users"
              className="px-4 py-2 bg-slate-900 hover:bg-slate-800 border border-slate-800 rounded-xl text-xs font-semibold text-slate-200 transition-colors"
            >
              User Management
            </Link>
            <Link
              to="/admin/disputes"
              className="px-4 py-2 bg-slate-900 hover:bg-slate-800 border border-slate-800 rounded-xl text-xs font-semibold text-rose-300 transition-colors"
            >
              Dispute Arbitration
            </Link>
            <Link
              to="/admin/reports"
              className="px-4 py-2 bg-slate-900 hover:bg-slate-800 border border-slate-800 rounded-xl text-xs font-semibold text-amber-300 transition-colors"
            >
              Content Moderation
            </Link>
            <Link
              to="/admin/jobs"
              className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold rounded-xl shadow-md transition-colors"
            >
              Ops Jobs
            </Link>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {statCards.map((sc, i) => (
            <div
              key={i}
              className="p-6 bg-slate-900/70 border border-slate-800/80 rounded-3xl space-y-3 shadow-xl"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-400">{sc.label}</span>
                <div className="p-2 bg-slate-950 rounded-xl border border-slate-800">{sc.icon}</div>
              </div>
              <div className="text-3xl font-black text-white font-mono tracking-tight">
                {sc.value}
              </div>
            </div>
          ))}
        </div>

        {/* Category Breakdown */}
        <div className="p-8 bg-slate-900/60 border border-slate-800/80 rounded-3xl space-y-6">
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <Layers className="w-5 h-5 text-emerald-400" />
            Category Inventory Distribution
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {Object.entries(stats.categoryItemCounts || {}).map(([cat, count]) => (
              <div
                key={cat}
                className="p-4 bg-slate-950/70 border border-slate-800 rounded-2xl flex items-center justify-between"
              >
                <span className="text-sm font-semibold text-slate-200">{cat}</span>
                <span className="px-3 py-1 bg-emerald-950 border border-emerald-500/30 text-emerald-400 font-mono font-bold text-xs rounded-xl">
                  {String(count)} items
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
