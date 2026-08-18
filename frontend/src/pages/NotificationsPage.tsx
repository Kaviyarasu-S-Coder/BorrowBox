import React, { useState, useEffect } from 'react';
import { Bell, CheckCheck, Sparkles, Box } from 'lucide-react';
import { notificationService } from '../services/notificationService';
import { Notification } from '../types';
import { useToast } from '../context/ToastContext';

export const NotificationsPage: React.FC = () => {
  const { success, error } = useToast();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  const loadNotifications = () => {
    setLoading(true);
    notificationService
      .getNotifications(0, 50)
      .then((res) => setNotifications(res.content))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      success('All notifications marked as read.');
      loadNotifications();
    } catch {
      error('Failed to update notifications.');
    }
  };

  const handleMarkAsRead = async (id: number) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true, isRead: true } : n))
      );
    } catch {}
  };

  const unreadExist = notifications.some((n) => !n.read && !n.isRead);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight">Notifications</h1>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Updates on borrow requests, OTP handovers, return deadlines, and community activity.
            </p>
          </div>

          {unreadExist && (
            <button
              onClick={handleMarkAllAsRead}
              className="flex items-center gap-1.5 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-emerald-400 text-xs font-semibold rounded-xl border border-slate-800 transition-colors"
            >
              <CheckCheck className="w-4 h-4" />
              Mark All as Read
            </button>
          )}
        </div>

        {loading ? (
          <div className="text-center py-20 text-slate-500">Loading notifications...</div>
        ) : notifications.length > 0 ? (
          <div className="space-y-3">
            {notifications.map((n) => {
              const isRead = n.read || n.isRead;
              return (
                <div
                  key={n.id}
                  onClick={() => !isRead && handleMarkAsRead(n.id)}
                  className={`p-5 rounded-2xl border transition-all cursor-pointer ${
                    isRead
                      ? 'bg-slate-900/40 border-slate-800/60 opacity-70'
                      : 'bg-slate-900 border-emerald-500/30 shadow-lg shadow-emerald-950/20'
                  }`}
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-white">{n.title}</span>
                        {!isRead && (
                          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                        )}
                      </div>
                      <p className="text-xs text-slate-300 leading-relaxed">{n.message || n.content}</p>
                    </div>
                    <span className="text-[10px] text-slate-500 shrink-0 font-mono">
                      {new Date(n.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="text-center py-20 bg-slate-900/40 border border-slate-800 rounded-3xl space-y-3">
            <Bell className="w-12 h-12 text-slate-600 mx-auto" />
            <h3 className="text-lg font-bold text-white">No notifications</h3>
            <p className="text-xs text-slate-400">You're all caught up!</p>
          </div>
        )}
      </div>
    </div>
  );
};
