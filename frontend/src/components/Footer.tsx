import React from 'react';
import { Link } from 'react-router-dom';
import { Box, ShieldCheck, HeartHandshake, Sparkles, Lock } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-950 border-t border-slate-900 text-slate-400 text-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 lg:py-16">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 lg:gap-12">
          {/* Col 1: Platform Vision */}
          <div className="space-y-4 md:col-span-1">
            <Link to="/" className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center shadow-md shadow-emerald-500/20">
                <Box className="w-5 h-5 text-slate-950 stroke-[2.5]" />
              </div>
              <span className="text-xl font-bold tracking-tight text-white">
                Borrow<span className="text-emerald-400">Box</span>
              </span>
            </Link>
            <p className="text-xs text-slate-400 leading-relaxed">
              "Borrow what you need. Share what you have."
              <br />
              The trusted peer-to-peer sharing ecosystem designed to reduce waste, save money, and empower local communities.
            </p>
            <div className="flex items-center gap-3 pt-2 text-xs text-emerald-400">
              <ShieldCheck className="w-4 h-4" />
              <span>OTP Handover Verified & Insured</span>
            </div>
          </div>

          {/* Col 2: Categories */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-200 mb-4">
              Popular Gear
            </h4>
            <ul className="space-y-2 text-xs">
              <li>
                <Link to="/explore?category=power-tools" className="hover:text-emerald-400 transition-colors">
                  Power Tools & Workshop
                </Link>
              </li>
              <li>
                <Link to="/explore?category=cameras-photography" className="hover:text-emerald-400 transition-colors">
                  Cameras & Photography
                </Link>
              </li>
              <li>
                <Link to="/explore?category=camping-outdoors" className="hover:text-emerald-400 transition-colors">
                  Camping & Outdoors
                </Link>
              </li>
              <li>
                <Link to="/explore?category=musical-instruments" className="hover:text-emerald-400 transition-colors">
                  Musical Instruments
                </Link>
              </li>
              <li>
                <Link to="/explore?category=lawn-garden" className="hover:text-emerald-400 transition-colors">
                  Lawn & Gardening
                </Link>
              </li>
            </ul>
          </div>

          {/* Col 3: Trust & Safety */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-200 mb-4">
              Trust & Safety
            </h4>
            <ul className="space-y-2 text-xs">
              <li className="flex items-center gap-1.5">
                <Lock className="w-3.5 h-3.5 text-emerald-400" />
                <span>Security Deposit Escrow</span>
              </li>
              <li className="flex items-center gap-1.5">
                <HeartHandshake className="w-3.5 h-3.5 text-emerald-400" />
                <span>Weighted Reputation Scores</span>
              </li>
              <li className="flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-emerald-400" />
                <span>Condition Check Logs</span>
              </li>
              <li>
                <Link to="/explore" className="hover:text-emerald-400 transition-colors">
                  Community Guidelines
                </Link>
              </li>
              <li>
                <Link to="/explore" className="hover:text-emerald-400 transition-colors">
                  Dispute Resolution Policy
                </Link>
              </li>
            </ul>
          </div>

          {/* Col 4: Demo Credentials & Info */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-200 mb-4">
              Quick Connect
            </h4>
            <p className="text-xs text-slate-400 leading-relaxed mb-3">
              Explore live lending and borrowing flows instantly with verified community test accounts.
            </p>
            <div className="p-3 bg-slate-900/70 border border-slate-800 rounded-xl text-[11px] space-y-1">
              <div className="text-slate-300 font-semibold">Demo Accounts:</div>
              <div className="text-slate-400">Admin: <span className="text-amber-400 font-mono">admin@borrowbox.com</span></div>
              <div className="text-slate-400">User: <span className="text-emerald-400 font-mono">alex@borrowbox.test</span></div>
              <div className="text-slate-500">Pass: <span className="font-mono text-slate-400">Password123!</span></div>
            </div>
          </div>
        </div>

        <div className="mt-12 pt-8 border-t border-slate-900 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500 gap-4">
          <p>© {new Date().getFullYear()} BorrowBox Platform. All rights reserved.</p>
          <div className="flex items-center gap-6">
            <Link to="/explore" className="hover:text-slate-400">Privacy Policy</Link>
            <Link to="/explore" className="hover:text-slate-400">Terms of Service</Link>
            <Link to="/explore" className="hover:text-slate-400">Security Architecture</Link>
          </div>
        </div>
      </div>
    </footer>
  );
};
