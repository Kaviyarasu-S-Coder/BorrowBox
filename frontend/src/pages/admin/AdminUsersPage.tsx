import React, { useState, useEffect } from 'react';
import { ShieldCheck, UserX, UserCheck, ShieldAlert, Search } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { AdminUser } from '../../types';
import { useToast } from '../../context/ToastContext';

export const AdminUsersPage: React.FC = () => {
  const { success, error } = useToast();
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [search, setSearch] = useState<string>('');

  const loadUsers = () => {
    setLoading(true);
    adminService
      .getUsers(0, 100)
      .then((res) => setUsers(res.content))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleToggleStatus = async (userId: number) => {
    try {
      const res = await adminService.toggleUserStatus(userId);
      success(`User account status set to: ${res.active ? 'ACTIVE' : 'BANNED'}`);
      loadUsers();
    } catch {
      error('Failed to change user status.');
    }
  };

  const handleToggleVerify = async (userId: number) => {
    try {
      const res = await adminService.toggleUserVerify(userId);
      success(`User verification set to: ${res.verified ? 'VERIFIED' : 'UNVERIFIED'}`);
      loadUsers();
    } catch {
      error('Failed to update verification status.');
    }
  };

  const filteredUsers = users.filter(
    (u) =>
      u.fullName.toLowerCase().includes(search.toLowerCase()) ||
      u.email.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight">User Moderation</h1>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Manage member reputation, identity verification status, and ban/unban privileges.
            </p>
          </div>

          <div className="relative w-72">
            <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by name or email..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-emerald-500"
            />
          </div>
        </div>

        {/* Users Table */}
        <div className="bg-slate-900/80 border border-slate-800 rounded-3xl overflow-hidden shadow-xl">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-950/80 border-b border-slate-800 text-slate-400 font-bold uppercase tracking-wider">
                <tr>
                  <th className="p-4">User</th>
                  <th className="p-4">Role</th>
                  <th className="p-4">Reputation</th>
                  <th className="p-4">Borrows / Lends</th>
                  <th className="p-4">Verification</th>
                  <th className="p-4">Status</th>
                  <th className="p-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {filteredUsers.map((u) => {
                  const isAdmin = u.roles?.includes('ROLE_ADMIN') || u.role === 'ROLE_ADMIN';
                  return (
                    <tr key={u.id} className="hover:bg-slate-800/40 transition-colors">
                      <td className="p-4">
                        <div className="font-bold text-white">{u.fullName}</div>
                        <div className="text-slate-500 text-[11px]">{u.email}</div>
                      </td>
                      <td className="p-4">
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                            isAdmin
                              ? 'bg-amber-950 text-amber-300 border border-amber-500/30'
                              : 'bg-slate-800 text-slate-300'
                          }`}
                        >
                          {isAdmin ? 'ADMIN' : 'USER'}
                        </span>
                      </td>
                      <td className="p-4 font-mono font-bold text-emerald-400">
                        {u.reputationScore}
                      </td>
                      <td className="p-4 text-slate-400">
                        {u.completedBorrowings}B / {u.completedLendings}L
                      </td>
                      <td className="p-4">
                        <button
                          onClick={() => handleToggleVerify(u.id)}
                          className={`px-2.5 py-1 rounded-xl text-[11px] font-semibold border transition-colors ${
                            u.verified
                              ? 'bg-cyan-950/60 border-cyan-500/30 text-cyan-300'
                              : 'bg-slate-950 border-slate-800 text-slate-500 hover:text-slate-300'
                          }`}
                        >
                          {u.verified ? '✓ Verified' : 'Unverified'}
                        </button>
                      </td>
                      <td className="p-4">
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                            u.active ? 'bg-emerald-950 text-emerald-300' : 'bg-rose-950 text-rose-300'
                          }`}
                        >
                          {u.active ? 'ACTIVE' : 'BANNED'}
                        </span>
                      </td>
                      <td className="p-4 text-right">
                        <button
                          onClick={() => handleToggleStatus(u.id)}
                          className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors ${
                            u.active
                              ? 'bg-rose-950/50 hover:bg-rose-900 border border-rose-500/30 text-rose-300'
                              : 'bg-emerald-950/50 hover:bg-emerald-900 border border-emerald-500/30 text-emerald-300'
                          }`}
                        >
                          {u.active ? 'Ban User' : 'Unban'}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};
