import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Box,
  PlusCircle,
  Heart,
  Bell,
  MessageSquare,
  User,
  LogOut,
  ShieldCheck,
  Menu,
  X,
  Compass,
  ArrowRightLeft,
  Share2,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { notificationService } from '../services/notificationService';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [userMenuOpen, setUserMenuOpen] = useState<boolean>(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);

  useEffect(() => {
    if (isAuthenticated) {
      notificationService
        .getUnreadCount()
        .then(setUnreadCount)
        .catch(() => {});
      const interval = setInterval(() => {
        notificationService
          .getUnreadCount()
          .then(setUnreadCount)
          .catch(() => {});
      }, 30000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated]);

  const handleLogout = () => {
    logout();
    navigate('/login');
    setUserMenuOpen(false);
  };

  return (
    <nav className="sticky top-0 z-40 bg-slate-950/80 backdrop-blur-md border-b border-slate-800/80 transition-all">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Brand Logo */}
          <Link to="/" className="flex items-center gap-2.5 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center shadow-lg shadow-emerald-500/20 group-hover:scale-105 transition-transform duration-200">
              <Box className="w-6 h-6 text-slate-950 stroke-[2.5]" />
            </div>
            <div>
              <span className="text-xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-300 bg-clip-text text-transparent">
                Borrow<span className="text-emerald-400">Box</span>
              </span>
              <span className="hidden sm:block text-[10px] text-emerald-500/90 font-medium tracking-wide uppercase">
                Peer-to-Peer Sharing
              </span>
            </div>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-1.5">
            <Link
              to="/explore"
              className="flex items-center gap-1.5 px-3.5 py-2 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/60 rounded-lg transition-colors"
            >
              <Compass className="w-4 h-4 text-slate-400" />
              Explore Items
            </Link>

            {isAuthenticated && (
              <>
                <Link
                  to="/borrows"
                  className="flex items-center gap-1.5 px-3.5 py-2 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/60 rounded-lg transition-colors"
                >
                  <ArrowRightLeft className="w-4 h-4 text-slate-400" />
                  My Borrows
                </Link>

                <Link
                  to="/lends"
                  className="flex items-center gap-1.5 px-3.5 py-2 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/60 rounded-lg transition-colors"
                >
                  <Share2 className="w-4 h-4 text-slate-400" />
                  My Lends
                </Link>
              </>
            )}
          </div>

          {/* Right Action Icons & Auth Profile */}
          <div className="hidden md:flex items-center gap-3">
            {isAuthenticated ? (
              <>
                <Link
                  to="/items/create"
                  className="flex items-center gap-2 px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-sm font-semibold rounded-lg shadow-sm shadow-emerald-500/25 hover:shadow-emerald-500/40 transition-all"
                >
                  <PlusCircle className="w-4 h-4 stroke-[2.5]" />
                  List an Item
                </Link>

                <Link
                  to="/favorites"
                  className="p-2 text-slate-400 hover:text-rose-400 hover:bg-slate-800/60 rounded-lg transition-colors relative"
                  title="Watchlist"
                >
                  <Heart className="w-5 h-5" />
                </Link>

                <Link
                  to="/messages"
                  className="p-2 text-slate-400 hover:text-emerald-400 hover:bg-slate-800/60 rounded-lg transition-colors relative"
                  title="Messages"
                >
                  <MessageSquare className="w-5 h-5" />
                </Link>

                <Link
                  to="/notifications"
                  className="p-2 text-slate-400 hover:text-amber-400 hover:bg-slate-800/60 rounded-lg transition-colors relative"
                  title="Notifications"
                >
                  <Bell className="w-5 h-5" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1 right-1 w-2.5 h-2.5 bg-rose-500 rounded-full ring-2 ring-slate-950" />
                  )}
                </Link>

                {/* User Dropdown */}
                <div className="relative">
                  <button
                    onClick={() => setUserMenuOpen(!userMenuOpen)}
                    className="flex items-center gap-2 p-1.5 rounded-xl hover:bg-slate-800/80 border border-slate-800 transition-colors"
                  >
                    <div className="w-8 h-8 rounded-lg bg-emerald-950 text-emerald-400 border border-emerald-500/30 flex items-center justify-center font-bold text-sm">
                      {user?.fullName?.charAt(0) || 'U'}
                    </div>
                    <div className="text-left">
                      <div className="text-xs font-semibold text-slate-200 leading-none">
                        {user?.fullName?.split(' ')[0]}
                      </div>
                      <div className="text-[10px] text-emerald-400 font-mono mt-0.5">
                        ⭐ {user?.reputationScore || 80} Rep
                      </div>
                    </div>
                  </button>

                  {userMenuOpen && (
                    <div
                      className="absolute right-0 mt-2 w-56 bg-slate-900 border border-slate-800 rounded-xl shadow-2xl py-1.5 z-50 animate-in fade-in slide-in-from-top-2"
                      onClick={() => setUserMenuOpen(false)}
                    >
                      <div className="px-3.5 py-2 border-b border-slate-800">
                        <div className="text-sm font-semibold text-white">{user?.fullName}</div>
                        <div className="text-xs text-slate-400 truncate">{user?.email}</div>
                      </div>

                      {isAdmin && (
                        <Link
                          to="/admin"
                          className="flex items-center gap-2.5 px-3.5 py-2 text-xs font-medium text-amber-400 hover:bg-slate-800 transition-colors"
                        >
                          <ShieldCheck className="w-4 h-4" />
                          Admin Console
                        </Link>
                      )}

                      <Link
                        to="/profile"
                        className="flex items-center gap-2.5 px-3.5 py-2 text-xs font-medium text-slate-300 hover:text-white hover:bg-slate-800 transition-colors"
                      >
                        <User className="w-4 h-4 text-slate-400" />
                        My Profile & Badges
                      </Link>

                      <Link
                        to="/my-items"
                        className="flex items-center gap-2.5 px-3.5 py-2 text-xs font-medium text-slate-300 hover:text-white hover:bg-slate-800 transition-colors"
                      >
                        <Box className="w-4 h-4 text-slate-400" />
                        My Listed Gear
                      </Link>

                      <div className="border-t border-slate-800 my-1" />

                      <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-2.5 px-3.5 py-2 text-xs font-medium text-rose-400 hover:bg-slate-800 transition-colors"
                      >
                        <LogOut className="w-4 h-4" />
                        Sign Out
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex items-center gap-2.5">
                <Link
                  to="/login"
                  className="px-4 py-2 text-sm font-semibold text-slate-300 hover:text-white hover:bg-slate-800/60 rounded-lg transition-colors"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-sm font-semibold rounded-lg shadow-sm shadow-emerald-500/25 hover:shadow-emerald-500/40 transition-all"
                >
                  Get Started
                </Link>
              </div>
            )}
          </div>

          {/* Mobile menu toggle */}
          <div className="flex md:hidden items-center gap-2">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden border-b border-slate-800 bg-slate-950/95 px-4 pt-2 pb-4 space-y-2">
          <Link
            to="/explore"
            onClick={() => setMobileMenuOpen(false)}
            className="flex items-center gap-2.5 px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 rounded-lg"
          >
            <Compass className="w-4 h-4 text-emerald-400" />
            Explore Gear
          </Link>
          {isAuthenticated ? (
            <>
              <Link
                to="/items/create"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center gap-2.5 px-3 py-2 text-sm font-medium text-emerald-400 hover:bg-slate-800 rounded-lg"
              >
                <PlusCircle className="w-4 h-4" />
                List an Item
              </Link>
              <Link
                to="/borrows"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center gap-2.5 px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 rounded-lg"
              >
                <ArrowRightLeft className="w-4 h-4 text-emerald-400" />
                My Borrows
              </Link>
              <Link
                to="/lends"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center gap-2.5 px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 rounded-lg"
              >
                <Share2 className="w-4 h-4 text-emerald-400" />
                My Lends
              </Link>
              <Link
                to="/favorites"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center gap-2.5 px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 rounded-lg"
              >
                <Heart className="w-4 h-4 text-rose-400" />
                Watchlist
              </Link>
              <Link
                to="/messages"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center gap-2.5 px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 rounded-lg"
              >
                <MessageSquare className="w-4 h-4 text-emerald-400" />
                Messages
              </Link>
              <Link
                to="/profile"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center gap-2.5 px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 rounded-lg"
              >
                <User className="w-4 h-4 text-slate-400" />
                Profile
              </Link>
              {isAdmin && (
                <Link
                  to="/admin"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center gap-2.5 px-3 py-2 text-sm font-medium text-amber-400 hover:bg-slate-800 rounded-lg"
                >
                  <ShieldCheck className="w-4 h-4" />
                  Admin Console
                </Link>
              )}
              <button
                onClick={() => {
                  handleLogout();
                  setMobileMenuOpen(false);
                }}
                className="w-full flex items-center gap-2.5 px-3 py-2 text-sm font-medium text-rose-400 hover:bg-slate-800 rounded-lg"
              >
                <LogOut className="w-4 h-4" />
                Sign Out
              </button>
            </>
          ) : (
            <div className="pt-2 flex flex-col gap-2">
              <Link
                to="/login"
                onClick={() => setMobileMenuOpen(false)}
                className="w-full text-center py-2 text-sm font-semibold text-slate-300 bg-slate-900 border border-slate-800 rounded-lg"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                onClick={() => setMobileMenuOpen(false)}
                className="w-full text-center py-2 text-sm font-semibold text-slate-950 bg-emerald-500 rounded-lg"
              >
                Get Started
              </Link>
            </div>
          )}
        </div>
      )}
    </nav>
  );
};
