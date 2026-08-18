import React, { useState, useEffect } from 'react';
import { AlertTriangle, CheckCircle2, ShieldCheck, X } from 'lucide-react';
import { disputeService } from '../../services/disputeService';
import { Dispute } from '../../types';
import { useToast } from '../../context/ToastContext';

export const AdminDisputesPage: React.FC = () => {
  const { success, error } = useToast();
  const [disputes, setDisputes] = useState<Dispute[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Resolution modal
  const [resolveModalOpen, setResolveModalOpen] = useState<boolean>(false);
  const [selectedDispute, setSelectedDispute] = useState<Dispute | null>(null);
  const [resolutionNotes, setResolutionNotes] = useState<string>('');
  const [refundEscrow, setRefundEscrow] = useState<boolean>(true);
  const [penalizeUser, setPenalizeUser] = useState<boolean>(false);
  const [submitting, setSubmitting] = useState<boolean>(false);

  const loadDisputes = () => {
    setLoading(true);
    disputeService
      .getAdminDisputes(0, 50)
      .then((res) => setDisputes(res.content))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadDisputes();
  }, []);

  const handleOpenResolve = (d: Dispute) => {
    setSelectedDispute(d);
    setResolutionNotes('');
    setRefundEscrow(true);
    setPenalizeUser(false);
    setResolveModalOpen(true);
  };

  const handleResolveSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedDispute || !resolutionNotes.trim()) return;

    setSubmitting(true);
    try {
      await disputeService.resolveDispute(selectedDispute.id, {
        resolutionNotes: resolutionNotes.trim(),
        refundEscrow,
        penalizeUser,
      });
      success('Dispute resolved successfully.');
      setResolveModalOpen(false);
      loadDisputes();
    } catch {
      error('Failed to resolve dispute.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
        <div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Dispute Arbitration Console</h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Arbitrate transaction conflicts, authorize security deposit release, and enforce reputation penalties.
          </p>
        </div>

        {loading ? (
          <div className="text-center py-20 text-slate-500">Loading disputes...</div>
        ) : disputes.length > 0 ? (
          <div className="space-y-4">
            {disputes.map((d) => (
              <div
                key={d.id}
                className="p-6 bg-slate-900/70 border border-slate-800 rounded-3xl flex flex-col md:flex-row items-start md:items-center justify-between gap-6"
              >
                <div className="space-y-2 flex-1">
                  <div className="flex items-center gap-3">
                    <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold ${
                      d.status === 'OPEN'
                        ? 'bg-rose-950/80 border border-rose-500/40 text-rose-300'
                        : 'bg-emerald-950 text-emerald-300'
                    }`}>
                      {d.status}
                    </span>
                    <span className="text-xs text-slate-500 font-mono">Dispute #{d.id}</span>
                    <span className="text-xs text-slate-400">Tx #{d.transactionId}</span>
                  </div>

                  <h3 className="font-bold text-white text-base">
                    Reason: {d.reason.replace('_', ' ')}
                  </h3>

                  <div className="text-xs text-slate-400">
                    Raised by: <strong className="text-slate-200">{d.raisedByName}</strong>
                  </div>

                  <p className="text-xs text-slate-300 bg-slate-950/70 p-3 rounded-2xl border border-slate-800">
                    "{d.description}"
                  </p>

                  {d.resolutionNotes && (
                    <div className="text-xs text-emerald-400 bg-emerald-950/30 p-2.5 rounded-xl border border-emerald-500/20">
                      <strong>Resolution:</strong> {d.resolutionNotes}
                    </div>
                  )}
                </div>

                {d.status === 'OPEN' && (
                  <button
                    onClick={() => handleOpenResolve(d)}
                    className="px-5 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-xs rounded-xl shadow-lg self-stretch md:self-auto"
                  >
                    Arbitrate & Resolve
                  </button>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-20 bg-slate-900/40 border border-slate-800 rounded-3xl space-y-3">
            <AlertTriangle className="w-12 h-12 text-slate-600 mx-auto" />
            <h3 className="text-lg font-bold text-white">No active disputes</h3>
            <p className="text-xs text-slate-400">All transactions are running smoothly.</p>
          </div>
        )}
      </div>

      {/* Resolution Modal */}
      {resolveModalOpen && selectedDispute && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-base font-bold text-white">Resolve Dispute #{selectedDispute.id}</h3>
              <button onClick={() => setResolveModalOpen(false)} className="p-1 text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleResolveSubmit} className="space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-300 block mb-1">Arbitration Notes & Ruling</label>
                <textarea
                  rows={3}
                  required
                  placeholder="State the decision and evidence reviewed..."
                  value={resolutionNotes}
                  onChange={(e) => setResolutionNotes(e.target.value)}
                  className="w-full p-2.5 bg-slate-950 border border-slate-800 rounded-xl text-white"
                />
              </div>

              <div className="space-y-2 pt-1">
                <label className="flex items-center gap-2 cursor-pointer text-slate-300">
                  <input
                    type="checkbox"
                    checked={refundEscrow}
                    onChange={(e) => setRefundEscrow(e.target.checked)}
                    className="rounded accent-emerald-500"
                  />
                  <span>Refund Deposit Escrow to Borrower</span>
                </label>

                <label className="flex items-center gap-2 cursor-pointer text-rose-300">
                  <input
                    type="checkbox"
                    checked={penalizeUser}
                    onChange={(e) => setPenalizeUser(e.target.checked)}
                    className="rounded accent-rose-500"
                  />
                  <span>Apply -10 Reputation Penalty to at-fault party</span>
                </label>
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
                  className="px-5 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded-xl"
                >
                  {submitting ? 'Resolving...' : 'Confirm Resolution'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
