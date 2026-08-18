import React, { useState, useEffect } from 'react';
import {
  User,
  ShieldCheck,
  Award,
  Star,
  MapPin,
  Mail,
  Phone,
  Calendar,
  Sparkles,
  CheckCircle2,
  AlertTriangle,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { ratingService } from '../services/ratingService';
import { Rating } from '../types';
import { RatingStars } from '../components/RatingStars';

export const ProfilePage: React.FC = () => {
  const { user, profile, updateProfile } = useAuth();
  const { success, error } = useToast();

  const [ratings, setRatings] = useState<Rating[]>([]);
  const [editing, setEditing] = useState<boolean>(false);

  // Edit fields
  const [fullName, setFullName] = useState<string>('');
  const [bio, setBio] = useState<string>('');
  const [phone, setPhone] = useState<string>('');
  const [location, setLocation] = useState<string>('');
  const [saving, setSaving] = useState<boolean>(false);

  useEffect(() => {
    if (profile) {
      setFullName(profile.fullName || '');
      setBio(profile.bio || '');
      setPhone(profile.phone || '');
      setLocation(profile.location || '');

      ratingService
        .getUserRatings(profile.id, 0, 20)
        .then((res) => setRatings(res.content))
        .catch(() => {});
    }
  }, [profile]);

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await updateProfile({
        fullName: fullName.trim(),
        bio: bio.trim() || undefined,
        phone: phone.trim() || undefined,
        location: location.trim() || undefined,
      });
      success('Profile updated successfully!');
      setEditing(false);
    } catch {
      error('Failed to update profile.');
    } finally {
      setSaving(false);
    }
  };

  const reputation = profile?.reputationScore || 80.0;
  const badgeLevel =
    reputation >= 90
      ? { title: 'Master Lender & Trusted Borrow Partner', color: 'text-amber-400 bg-amber-950/60 border-amber-500/30' }
      : reputation >= 75
      ? { title: 'Verified Community Member', color: 'text-emerald-400 bg-emerald-950/60 border-emerald-500/30' }
      : { title: 'Community Participant', color: 'text-blue-400 bg-blue-950/60 border-blue-500/30' };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        {/* Profile Card Header */}
        <div className="p-8 bg-slate-900/80 border border-slate-800 rounded-3xl backdrop-blur-xl shadow-2xl flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="flex items-center gap-6">
            <div className="w-20 h-20 rounded-2xl bg-gradient-to-tr from-emerald-500 to-teal-400 text-slate-950 font-black text-3xl flex items-center justify-center shadow-xl shadow-emerald-500/20">
              {profile?.fullName?.charAt(0) || 'U'}
            </div>
            <div>
              <div className="flex items-center gap-3">
                <h1 className="text-2xl font-extrabold text-white">{profile?.fullName}</h1>
                {profile?.verified && (
                  <span className="flex items-center gap-1 text-xs font-bold text-cyan-400 bg-cyan-950/80 border border-cyan-500/30 px-2.5 py-0.5 rounded-full">
                    <ShieldCheck className="w-3.5 h-3.5" />
                    ID Verified
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-400 mt-1 flex items-center gap-3">
                <span>{profile?.email}</span>
                {profile?.location && (
                  <>
                    <span>•</span>
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3 h-3" />
                      {profile.location}
                    </span>
                  </>
                )}
              </p>
              <div className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold border mt-3 ${badgeLevel.color}`}>
                <Award className="w-3.5 h-3.5" />
                {badgeLevel.title}
              </div>
            </div>
          </div>

          <button
            onClick={() => setEditing(!editing)}
            className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold rounded-xl border border-slate-700 transition-colors self-stretch md:self-auto"
          >
            {editing ? 'Cancel' : 'Edit Profile'}
          </button>
        </div>

        {/* Edit Profile Form Modal / Inline */}
        {editing && (
          <form onSubmit={handleSaveProfile} className="p-6 bg-slate-900 border border-slate-800 rounded-3xl space-y-4">
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">Update Personal Profile</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-xs text-slate-400 block mb-1">Full Name</label>
                <input
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white"
                />
              </div>
              <div>
                <label className="text-xs text-slate-400 block mb-1">Location / Neighborhood</label>
                <input
                  type="text"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white"
                />
              </div>
              <div>
                <label className="text-xs text-slate-400 block mb-1">Phone Number</label>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white"
                />
              </div>
              <div>
                <label className="text-xs text-slate-400 block mb-1">Bio</label>
                <input
                  type="text"
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-white"
                />
              </div>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="submit"
                disabled={saving}
                className="px-5 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-xs rounded-xl"
              >
                {saving ? 'Saving...' : 'Save Profile'}
              </button>
            </div>
          </form>
        )}

        {/* Reputation Score Breakdown Card */}
        <div className="p-8 bg-slate-900/60 border border-slate-800/80 rounded-3xl space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-xs font-semibold text-emerald-400 uppercase tracking-wider">
                Community Trust Algorithm
              </div>
              <h2 className="text-xl font-bold text-white mt-0.5">
                Reputation Score: {reputation} / 100
              </h2>
            </div>
            <div className="text-3xl font-black text-emerald-400 font-mono">
              {reputation}
            </div>
          </div>

          {/* Metrics grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
            <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl">
              <div className="text-slate-500 mb-1">Completed Borrowings</div>
              <div className="text-lg font-bold text-white">{profile?.completedBorrowings || 0}</div>
            </div>
            <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl">
              <div className="text-slate-500 mb-1">Completed Lendings</div>
              <div className="text-lg font-bold text-white">{profile?.completedLendings || 0}</div>
            </div>
            <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl">
              <div className="text-slate-500 mb-1">Average Star Rating</div>
              <div className="text-lg font-bold text-amber-400">
                {profile?.averageRating ? profile.averageRating.toFixed(1) : '5.0'} ⭐
              </div>
            </div>
            <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl">
              <div className="text-slate-500 mb-1">Disputes / Penalties</div>
              <div className="text-lg font-bold text-emerald-400">
                {profile?.disputeCount || 0}
              </div>
            </div>
          </div>

          <p className="text-xs text-slate-400 leading-relaxed bg-slate-950/60 p-3.5 rounded-2xl border border-slate-800/80">
            💡 <strong>Algorithm Transparency:</strong> Reputation starts at 80.0 points. Increases with completed lendings (+2 pts), completed borrowings (+2 pts), and 5-star reviews. Decreases on late returns, cancellations (-5 pts), or upheld dispute claims (-10 pts).
          </p>
        </div>

        {/* Reviews Received */}
        <div className="p-8 bg-slate-900/60 border border-slate-800/80 rounded-3xl space-y-6">
          <h2 className="text-xl font-bold text-white">
            Reviews Received from Neighbors ({ratings.length})
          </h2>

          {ratings.length > 0 ? (
            <div className="space-y-4">
              {ratings.map((r) => (
                <div
                  key={r.id}
                  className="p-4 bg-slate-950/60 border border-slate-800 rounded-2xl text-xs space-y-2"
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <span className="font-bold text-slate-200">{r.fromUserName}</span>
                      <span className="text-slate-500 ml-2">for {r.itemTitle}</span>
                    </div>
                    <RatingStars rating={r.score} size="sm" showNumber />
                  </div>
                  {r.reviewComment && (
                    <p className="text-slate-300 italic">"{r.reviewComment}"</p>
                  )}
                  <div className="flex items-center gap-4 text-[10px] text-slate-500 pt-1">
                    <span>Communication: {r.communicationScore}★</span>
                    <span>Punctuality: {r.punctualityScore}★</span>
                    <span>Reliability: {r.reliabilityScore}★</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-12 text-slate-500 text-xs">
              No reviews yet. Complete your first borrow or lend transaction to build your rating!
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
