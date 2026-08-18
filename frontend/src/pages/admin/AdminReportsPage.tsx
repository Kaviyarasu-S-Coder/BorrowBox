import React, { useState, useEffect } from 'react';
import { Flag, CheckCircle2, AlertTriangle, X } from 'lucide-react';
import { disputeService } from '../../services/disputeService';
import { Report } from '../../types';
import { useToast } from '../../context/ToastContext';

export const AdminReportsPage: React.FC = () => {
  const { success, error } = useToast();
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Modal
  const [resolveModalOpen, setResolveModalOpen] = useState<boolean>(false);
  const [selectedReport, setSelectedReport] = useState<Report | null>(null);
  const [action, setAction] = useState<string>('DISMISSED');
  const [notes, setNotes] = useState<string>('');
  const [submitting, setSubmitting] = useState<boolean>(false);

  const loadReports = () => {
    setLoading(true);
    disputeService
      .getAdminReports(0, 50)
      .then((res) => setReports(res.content))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadReports();
  }, []);

  const handleOpenResolve = (r: Report) => {
    setSelectedReport(r);
    setAction('DISMISSED');
    setNotes('');
    setResolveModalOpen(true);
  };

  const handleResolveSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedReport) return;

    setSubmitting(true);
    try {
      await disputeService.resolveReport(selectedReport.id, {
        action,
        adminNotes: notes.trim() || undefined,
      });
      success('Report resolved.');
      setResolveModalOpen(false);
      loadReports();
    } catch {
      error('Failed to resolve report.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
        <div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Content Moderation Reports</h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Review user-submitted flags regarding prohibited items, misleading listings, or spam.
          </p>
        </div>

        {loading ? (
          <div className="text-center py-20 text-slate-500">Loading reports...</div>
        ) : reports.length > 0 ? (
          <div className="space-y-4">
            {reports.map((r) => (
              <div
                key={r.id}
                className="p-6 bg-slate-900/70 border border-slate-800 rounded-3xl flex flex-col md:flex-row items-start md:items-center justify-between gap-6"
              >
                <div className="space-y-2 flex-1">
                  <div className="flex items-center gap-3">
                    <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold ${
                      r.status === 'PENDING'
                        ? 'bg-amber-950/80 border border-amber-500/40 text-amber-300'
                        : 'bg-slate-800 text-slate-300'
                    }`}>
                      {r.status}
                    </span>
                    <span className="text-xs text-slate-500 font-mono">Report #{r.id}</span>
                  </div>

                  <h3 className="font-bold text-white text-base">
                    Reason: {r.reason.replace('_', ' ')}
                  </h3>

                  <div className="text-xs text-slate-400 flex items-center gap-4">
                    <span>Reporter: <strong className="text-slate-200">{r.reporterName}</strong></span>
                    {r.reportedItemId && <span>Item ID: <strong className="text-emerald-400">#{r.reportedItemId}</strong></span>}
                  </div>

                  <p className="text-xs text-slate-300 bg-slate-950/70 p-3 rounded-2xl border border-slate-800">
                    "{r.description}"
                  </p>
                </div>

                {r.status === 'PENDING' && (
                  <button
                    onClick={() => handleOpenResolve(r)}
                    className="px-5 py-2.5 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs rounded-xl shadow-lg self-stretch md:self-auto"
                  >
                    Moderate Report
                  </button>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-20 bg-slate-900/40 border border-slate-800 rounded-3xl space-y-3">
            <Flag className="w-12 h-12 text-slate-600 mx-auto" />
            <h3 className="text-lg font-bold text-white">No pending reports</h3>
            <p className="text-xs text-slate-400">No listings or users flagged for review.</p>
          </div>
        )}
      </div>

      {/* Resolve Modal */}
      {resolveModalOpen && selectedReport && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-base font-bold text-white">Moderate Report #{selectedReport.id}</h3>
              <button onClick={() => setResolveModalOpen(false)} className="p-1 text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleResolveSubmit} className="space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-300 block mb-1">Moderation Action</label>
                <select
                  value={action}
                  onChange={(e) => setAction(e.target.value)}
                  className="w-full p-2.5 bg-slate-950 border border-slate-800 rounded-xl text-white"
                >
                  <option value="DISMISSED">Dismiss Report (No Violation)</option>
                  <option value="ITEM_DEACTIVATED">Deactivate Flagged Item</option>
                  <option value="USER_BANNED">Ban Reported User</option>
                  <option value="WARNING_ISSUED">Issue Warning</option>
                </select>
              </div>

              <div>
                <label className="font-semibold text-slate-300 block mb-1">Admin Notes</label>
                <textarea
                  rows={3}
                  placeholder="Notes for internal moderation logs..."
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className="w-full p-2.5 bg-slate-950 border border-slate-800 rounded-xl text-white"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setResolveModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 font-semibold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold rounded-xl"
                >
                  {submitting ? 'Applying...' : 'Apply Action'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
