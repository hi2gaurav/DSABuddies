import React, { useEffect, useState } from 'react';
import { api } from '../../lib/api';
import { Announcement } from '../../types';
import { X, AlertTriangle, AlertCircle, Info } from 'lucide-react';
import { clsx } from 'clsx';
import { motion, AnimatePresence } from 'framer-motion';

export const AnnouncementBanner: React.FC = () => {
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);
  const [dismissedIds, setDismissedIds] = useState<number[]>(() => {
    try {
      const stored = localStorage.getItem('dsa_buddies_dismissed_announcements');
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    api.getActiveAnnouncements()
      .then(data => setAnnouncements(data))
      .catch(() => {});
  }, []);

  const handleDismiss = (id: number) => {
    const updated = [...dismissedIds, id];
    setDismissedIds(updated);
    try {
      localStorage.setItem('dsa_buddies_dismissed_announcements', JSON.stringify(updated));
    } catch {}
  };

  const visibleAnnouncements = announcements.filter(a => !dismissedIds.includes(a.id));

  if (visibleAnnouncements.length === 0) return null;

  return (
    <div className="space-y-3 mb-6">
      <AnimatePresence>
        {visibleAnnouncements.map((item) => {
          const isUrgent = item.priority === 'URGENT';
          const isHigh = item.priority === 'HIGH';

          return (
            <motion.div
              key={item.id}
              initial={{ opacity: 0, y: -8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, height: 0 }}
              className={clsx(
                "relative flex items-start gap-3.5 p-4 rounded-xl border shadow-sm backdrop-blur-sm transition-all",
                isUrgent
                  ? "bg-rose-50/90 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900/60 text-rose-900 dark:text-rose-200"
                  : isHigh
                  ? "bg-amber-50/90 dark:bg-amber-950/40 border-amber-200 dark:border-amber-900/60 text-amber-900 dark:text-amber-200"
                  : "bg-blue-50/90 dark:bg-blue-950/40 border-blue-200 dark:border-blue-900/60 text-blue-900 dark:text-blue-200"
              )}
            >
              <div className="mt-0.5 flex-shrink-0">
                {isUrgent ? (
                  <AlertCircle className="w-5 h-5 text-rose-500 animate-pulse" />
                ) : isHigh ? (
                  <AlertTriangle className="w-5 h-5 text-amber-500" />
                ) : (
                  <Info className="w-5 h-5 text-blue-500" />
                )}
              </div>

              <div className="flex-1 min-w-0 pr-6">
                <div className="flex items-center gap-2 flex-wrap">
                  <h4 className="font-semibold text-sm">{item.title}</h4>
                  <span className={clsx(
                    "text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full",
                    isUrgent
                      ? "bg-rose-100 text-rose-700 dark:bg-rose-900/60 dark:text-rose-300"
                      : isHigh
                      ? "bg-amber-100 text-amber-700 dark:bg-amber-900/60 dark:text-amber-300"
                      : "bg-blue-100 text-blue-700 dark:bg-blue-900/60 dark:text-blue-300"
                  )}>
                    {item.priority}
                  </span>
                  <span className="text-xs opacity-70">
                    by {item.authorName} • {new Date(item.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <p className="text-sm mt-1 leading-relaxed opacity-90 whitespace-pre-wrap">{item.message}</p>
              </div>

              <button
                onClick={() => handleDismiss(item.id)}
                className="absolute top-3 right-3 p-1 rounded-lg opacity-60 hover:opacity-100 hover:bg-black/5 dark:hover:bg-white/10 transition-colors"
                title="Dismiss announcement"
              >
                <X className="w-4 h-4" />
              </button>
            </motion.div>
          );
        })}
      </AnimatePresence>
    </div>
  );
};
