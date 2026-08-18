import React, { useState } from 'react';
import { Play, Clock, CheckCircle2, AlertTriangle, ShieldCheck, Zap } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { useToast } from '../../context/ToastContext';

export const AdminJobsPage: React.FC = () => {
  const { success, error } = useToast();
  const [runningJob, setRunningJob] = useState<string | null>(null);
  const [lastResults, setLastResults] = useState<Record<string, string>>({});

  const handleTriggerJob = async (jobKey: 'overdue' | 'reminders' | 'expired') => {
    setRunningJob(jobKey);
    try {
      let res: { message: string };
      if (jobKey === 'overdue') {
        res = await adminService.triggerOverdueJob();
      } else if (jobKey === 'reminders') {
        res = await adminService.triggerRemindersJob();
      } else {
        res = await adminService.triggerExpiredRequestsJob();
      }
      success(res.message);
      setLastResults((prev) => ({
        ...prev,
        [jobKey]: `${new Date().toLocaleTimeString()}: ${res.message}`,
      }));
    } catch {
      error('Failed to trigger background job.');
    } finally {
      setRunningJob(null);
    }
  };

  const jobs = [
    {
      key: 'overdue' as const,
      title: 'Scan & Mark Overdue Transactions',
      cron: '0 0 * * * * (Hourly)',
      desc: 'Checks active BORROWED transactions past their end date, transitions status to OVERDUE, applies automatic -5 reputation penalty, and dispatches urgent return notifications.',
    },
    {
      key: 'reminders' as const,
      title: 'Send 24-Hour Return Reminders',
      cron: '0 0 8 * * * (Daily at 08:00 AM)',
      desc: 'Scans for all transactions ending tomorrow and sends friendly return reminders and prep instructions to borrowers.',
    },
    {
      key: 'expired' as const,
      title: 'Auto-Cancel Stale Borrow Requests',
      cron: '0 */30 * * * * (Every 30 mins)',
      desc: 'Automatically cancels PENDING borrow requests where the requested start date has passed without owner approval.',
    },
  ];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        <div>
          <div className="inline-flex items-center gap-1.5 text-xs font-bold text-emerald-400 uppercase tracking-wider mb-1">
            <Zap className="w-4 h-4" /> Operations & Reliability
          </div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">
            Background Scheduled Jobs
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Spring Boot @Scheduled workers manage platform state automatically. Trigger jobs on-demand here for testing or maintenance.
          </p>
        </div>

        <div className="space-y-4">
          {jobs.map((j) => (
            <div
              key={j.key}
              className="p-6 bg-slate-900/70 border border-slate-800 rounded-3xl space-y-4 shadow-xl"
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h3 className="text-lg font-bold text-white">{j.title}</h3>
                  <div className="flex items-center gap-2 text-xs text-slate-400 mt-1">
                    <Clock className="w-3.5 h-3.5 text-emerald-400" />
                    <span className="font-mono text-slate-300">{j.cron}</span>
                  </div>
                </div>

                <button
                  onClick={() => handleTriggerJob(j.key)}
                  disabled={runningJob === j.key}
                  className="px-5 py-2.5 bg-emerald-500 hover:bg-emerald-400 disabled:opacity-50 text-slate-950 font-bold text-xs rounded-xl shadow-md flex items-center justify-center gap-2 shrink-0"
                >
                  <Play className="w-3.5 h-3.5 fill-slate-950" />
                  {runningJob === j.key ? 'Executing...' : 'Trigger Now'}
                </button>
              </div>

              <p className="text-xs text-slate-400 leading-relaxed">{j.desc}</p>

              {lastResults[j.key] && (
                <div className="p-3 bg-slate-950 border border-emerald-500/30 rounded-xl text-xs text-emerald-300 font-mono flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>{lastResults[j.key]}</span>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
