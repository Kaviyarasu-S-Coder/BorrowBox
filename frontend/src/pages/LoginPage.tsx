import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Box, Lock, Mail, ArrowRight, ShieldCheck, UserCheck, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const { success, error } = useToast();
  const navigate = useNavigate();

  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login({ email, password });
      success('Welcome back to BorrowBox!');
      navigate('/');
    } catch (err: any) {
      error(err.response?.data?.message || 'Invalid email or password.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = (demoEmail: string, demoPass: string) => {
    setEmail(demoEmail);
    setPassword(demoPass);
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center py-12 sm:px-6 lg:px-8 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(16,185,129,0.12),rgba(255,255,255,0))]">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <Link to="/" className="inline-flex items-center gap-2.5 group">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center shadow-xl shadow-emerald-500/20 group-hover:scale-105 transition-transform">
            <Box className="w-7 h-7 text-slate-950 stroke-[2.5]" />
          </div>
        </Link>
        <h2 className="mt-4 text-3xl font-extrabold tracking-tight text-white">
          Sign in to BorrowBox
        </h2>
        <p className="mt-2 text-xs text-slate-400">
          "Borrow what you need. Share what you have."
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4">
        <div className="bg-slate-900/80 border border-slate-800 backdrop-blur-xl py-8 px-6 sm:px-10 rounded-3xl shadow-2xl space-y-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                Email Address
              </label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  required
                  placeholder="name@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500 transition-colors"
                />
              </div>
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1.5">
                Password
              </label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  required
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-emerald-500 transition-colors"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3.5 bg-emerald-500 hover:bg-emerald-400 disabled:opacity-50 text-slate-950 font-bold rounded-xl shadow-lg shadow-emerald-500/25 transition-all text-sm flex items-center justify-center gap-2 mt-2"
            >
              {loading ? 'Authenticating...' : 'Sign In'}
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          {/* Demo Quick-Fill Accounts */}
          <div className="pt-4 border-t border-slate-800/80">
            <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2.5 text-center">
              1-Click Demo Login Credentials
            </div>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => handleQuickFill('admin@borrowbox.com', 'AdminPass123!')}
                className="p-2 bg-amber-950/30 hover:bg-amber-950/60 border border-amber-500/30 rounded-xl text-center text-[11px] font-semibold text-amber-300 transition-colors"
              >
                <ShieldCheck className="w-3.5 h-3.5 mx-auto mb-1 text-amber-400" />
                Admin
              </button>
              <button
                type="button"
                onClick={() => handleQuickFill('sarah@borrowbox.test', 'Password123!')}
                className="p-2 bg-emerald-950/30 hover:bg-emerald-950/60 border border-emerald-500/30 rounded-xl text-center text-[11px] font-semibold text-emerald-300 transition-colors"
              >
                <UserCheck className="w-3.5 h-3.5 mx-auto mb-1 text-emerald-400" />
                Owner
              </button>
              <button
                type="button"
                onClick={() => handleQuickFill('alex@borrowbox.test', 'Password123!')}
                className="p-2 bg-blue-950/30 hover:bg-blue-950/60 border border-blue-500/30 rounded-xl text-center text-[11px] font-semibold text-blue-300 transition-colors"
              >
                <UserCheck className="w-3.5 h-3.5 mx-auto mb-1 text-blue-400" />
                Borrower
              </button>
            </div>
          </div>

          {/* Sign Up Link */}
          <div className="text-center text-xs text-slate-400 pt-2">
            Don't have an account?{' '}
            <Link to="/register" className="text-emerald-400 font-semibold hover:underline">
              Create an account
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
