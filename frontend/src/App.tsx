import React from 'react';
import { Routes, Route, Link } from 'react-router-dom';
import { Package, Search, Heart, MessageSquare, ShieldCheck, User, PlusCircle } from 'lucide-react';

export default function App() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100 font-sans">
      {/* Navigation Header */}
      <header className="sticky top-0 z-50 bg-slate-900/80 backdrop-blur-md border-b border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 text-xl font-bold text-teal-400 tracking-tight">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-teal-500 to-emerald-400 flex items-center justify-center text-slate-950 font-black shadow-lg shadow-teal-500/20">
              <Package className="w-5 h-5 text-slate-950" />
            </div>
            <span>Borrow<span className="text-white">Box</span></span>
          </Link>

          <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-slate-300">
            <Link to="/items" className="hover:text-teal-400 transition-colors flex items-center gap-1.5">
              <Search className="w-4 h-4" /> Browse Items
            </Link>
            <Link to="/how-it-works" className="hover:text-teal-400 transition-colors flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4" /> How It Works
            </Link>
          </nav>

          <div className="flex items-center gap-3">
            <Link to="/items/create" className="btn-primary text-xs sm:text-sm py-2 px-3.5 flex items-center gap-1.5">
              <PlusCircle className="w-4 h-4" />
              <span>List an Item</span>
            </Link>
            <Link to="/login" className="btn-secondary text-xs sm:text-sm py-2 px-3.5">
              Sign In
            </Link>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Routes>
          <Route path="/" element={
            <div className="py-12 text-center max-w-3xl mx-auto">
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-teal-500/10 border border-teal-500/30 text-teal-400 text-xs font-semibold uppercase tracking-wider mb-6">
                🌱 Borrow what you need. Share what you have.
              </div>
              <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight text-white mb-6">
                Borrow Quality Items <br />
                <span className="gradient-text">From People Nearby</span>
              </h1>
              <p className="text-lg text-slate-400 mb-8 leading-relaxed">
                Save money, declutter your home, and reduce consumption. From DSLR cameras to power drills, get temporary access to everything you need.
              </p>
              <div className="flex flex-wrap items-center justify-center gap-4">
                <Link to="/items" className="btn-primary px-7 py-3 text-base">
                  Explore Catalog
                </Link>
                <Link to="/register" className="btn-secondary px-7 py-3 text-base">
                  Join Community
                </Link>
              </div>
            </div>
          } />
          <Route path="/items" element={<div className="p-8 text-center text-slate-400">Item Catalog Component (Phase 7)</div>} />
          <Route path="/login" element={<div className="p-8 text-center text-slate-400">Login Component (Phase 3)</div>} />
          <Route path="/register" element={<div className="p-8 text-center text-slate-400">Register Component (Phase 3)</div>} />
        </Routes>
      </main>

      {/* Footer */}
      <footer className="bg-slate-900/50 border-t border-slate-800/80 py-8 text-center text-xs text-slate-500">
        <p>© 2026 BorrowBox. Peer-to-Peer Borrowing & Lending Platform. Production-grade architecture.</p>
      </footer>
    </div>
  );
}
