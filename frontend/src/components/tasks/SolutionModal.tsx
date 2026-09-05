import React, { useState } from 'react';
import Modal from '../ui/Modal';
import Badge from '../ui/Badge';
import { Task } from '../../types';
import { ExternalLink, Star, Code, BookOpen } from 'lucide-react';
import { toSafeUrl } from '../../lib/security';

interface SolutionModalProps {
  task: Task | null;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (solutionLink: string, notes: string) => Promise<void>;
}

export const SolutionModal: React.FC<SolutionModalProps> = ({
  task,
  isOpen,
  onClose,
  onSubmit,
}) => {
  const [solutionLink, setSolutionLink] = useState('');
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!task) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await onSubmit(solutionLink.trim(), notes.trim());
      setSolutionLink('');
      setNotes('');
      onClose();
    } finally {
      setSubmitting(false);
    }
  };

  const handleQuickComplete = async () => {
    setSubmitting(true);
    try {
      await onSubmit('', '');
      setSolutionLink('');
      setNotes('');
      onClose();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Mark Problem as Solved 🎉">
      <div className="space-y-4">
        <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/60">
          <div className="flex items-start justify-between gap-3 mb-2">
            <h3 className="font-semibold text-gray-900 dark:text-white text-base">{task.title}</h3>
            <span className="text-xs font-bold text-amber-500 bg-amber-50 dark:bg-amber-950/40 px-2 py-0.5 rounded-full flex items-center gap-1 flex-shrink-0">
              <Star className="w-3 h-3 fill-current" /> +{task.xpReward} XP
            </span>
          </div>

          <div className="flex items-center gap-2 flex-wrap text-xs">
            <Badge variant={task.difficulty.toLowerCase() as any}>{task.difficulty}</Badge>
            <Badge color={task.topicColor}>{task.topicName}</Badge>
            {task.platformLink && (
              <a
                href={toSafeUrl(task.platformLink)}
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-600 dark:text-blue-400 hover:underline flex items-center gap-1"
              >
                <span>View Problem</span>
                <ExternalLink className="w-3 h-3" />
              </a>
            )}
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3">
          <div>
            <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1 flex items-center gap-1.5">
              <Code className="w-3.5 h-3.5 text-blue-500" />
              <span>Solution / Submission Link (Optional)</span>
            </label>
            <input
              type="url"
              placeholder="https://leetcode.com/submissions/detail/... or GitHub"
              value={solutionLink}
              onChange={(e) => setSolutionLink(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-lg border border-gray-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1 flex items-center gap-1.5">
              <BookOpen className="w-3.5 h-3.5 text-emerald-500" />
              <span>Takeaways / Complexity Notes (Optional)</span>
            </label>
            <textarea
              rows={3}
              placeholder="e.g. Used two pointers. Time: O(n), Space: O(1). Watch out for duplicates!"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-lg border border-gray-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 outline-none transition-all resize-none"
            />
          </div>

          <div className="pt-2 flex flex-col-reverse sm:flex-row items-center justify-end gap-2">
            <button
              type="button"
              onClick={handleQuickComplete}
              disabled={submitting}
              className="w-full sm:w-auto px-4 py-2 text-xs font-medium text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
            >
              Quick Complete (No Notes)
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="w-full sm:w-auto px-5 py-2 text-xs font-semibold rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white shadow-sm hover:shadow transition-all disabled:opacity-50"
            >
              {submitting ? 'Saving...' : 'Save & Earn XP 🚀'}
            </button>
          </div>
        </form>
      </div>
    </Modal>
  );
};

export default SolutionModal;
