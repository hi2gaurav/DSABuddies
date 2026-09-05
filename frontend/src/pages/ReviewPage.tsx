import React, { useEffect, useState } from 'react';
import { Brain, CheckCircle2, RotateCcw, ExternalLink, Star } from 'lucide-react';
import { motion } from 'framer-motion';
import { api } from '../lib/api';
import { ReviewItem } from '../types';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { triggerCelebrationConfetti } from '../lib/confetti';
import { toSafeUrl } from '../lib/security';
import NoteModal from '../components/common/NoteModal';

const ReviewPage: React.FC = () => {
  const [reviews, setReviews] = useState<ReviewItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [revealed, setRevealed] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [noteModalOpen, setNoteModalOpen] = useState(false);

  const fetchReviews = async () => {
    setLoading(true);
    try {
      const data = await api.getDueReviews();
      setReviews(data);
      setCurrentIndex(0);
      setRevealed(false);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, []);

  const currentItem = reviews[currentIndex];
  const isFinished = !loading && (reviews.length === 0 || currentIndex >= reviews.length);

  const handleRate = async (rating: number) => {
    if (!currentItem || submitting) return;
    setSubmitting(true);

    try {
      await api.submitReview(currentItem.taskId, rating);
      const nextIdx = currentIndex + 1;
      if (nextIdx >= reviews.length) {
        triggerCelebrationConfetti();
      }
      setCurrentIndex(nextIdx);
      setRevealed(false);
    } catch (err) {
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  // Keyboard shortcut listener
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (noteModalOpen) return;
      if (e.code === 'Space' && !revealed) {
        e.preventDefault();
        setRevealed(true);
      } else if (revealed && ['1', '2', '3', '4', '5'].includes(e.key)) {
        e.preventDefault();
        handleRate(parseInt(e.key, 10));
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [revealed, noteModalOpen, currentIndex, reviews]);

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center">
        <LoadingSpinner size="lg" />
        <p className="mt-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Loading Review Queue...</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="px-2.5 py-0.5 rounded-full bg-purple-500/10 border border-purple-500/20 text-purple-600 dark:text-purple-400 text-xs font-black tracking-wider uppercase inline-flex items-center gap-1">
              <Brain className="w-3 h-3 fill-current" /> SuperMemo SM-2
            </span>
          </div>
          <h1 className="text-3xl font-black text-gray-900 dark:text-white tracking-tight flex items-center gap-2">
            <span>Spaced Repetition Review</span>
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 dark:text-gray-400 mt-0.5">
            Strengthen long-term algorithmic recall with scientifically scheduled intervals
          </p>
        </div>

        {!isFinished && (
          <div className="flex items-center gap-2 text-xs font-bold bg-purple-50 dark:bg-purple-950/40 text-purple-700 dark:text-purple-300 px-3.5 py-1.5 rounded-full border border-purple-200 dark:border-purple-800/40">
            <span>Card {currentIndex + 1} of {reviews.length}</span>
          </div>
        )}
      </div>

      {/* Finished State */}
      {isFinished ? (
        <Card className="p-12 text-center bg-gradient-to-b from-white to-purple-50/20 dark:from-slate-850 dark:to-slate-900 border border-purple-100 dark:border-slate-800 shadow-xl">
          <div className="w-20 h-20 rounded-full bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center mx-auto mb-5 shadow-lg shadow-emerald-500/20">
            <CheckCircle2 className="w-10 h-10 text-white" />
          </div>
          <h2 className="text-2xl font-black text-gray-900 dark:text-white mb-2">
            Queue Clear! You're All Caught Up 🎉
          </h2>
          <p className="text-sm text-gray-500 dark:text-gray-400 max-w-md mx-auto mb-6">
            You've completed all scheduled problem reviews for today. New reviews will appear as intervals mature.
          </p>
          <div className="flex justify-center gap-3">
            <button
              onClick={fetchReviews}
              className="px-5 py-2.5 rounded-xl bg-gray-100 dark:bg-slate-800 hover:bg-gray-200 dark:hover:bg-slate-700 text-gray-800 dark:text-gray-200 text-xs font-bold flex items-center gap-2 transition-colors"
            >
              <RotateCcw className="w-4 h-4" /> Refresh Queue
            </button>
          </div>
        </Card>
      ) : (
        /* Active Flashcard Card */
        <div className="space-y-6">
          <motion.div
            key={currentItem.id}
            initial={{ opacity: 0, scale: 0.96, y: 15 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
          >
            <Card className="p-8 border border-gray-200/80 dark:border-slate-800 shadow-xl bg-white dark:bg-slate-850 relative overflow-hidden">
              <div className="flex items-start justify-between gap-4 mb-4">
                <div className="flex items-center gap-2 flex-wrap">
                  <Badge variant={currentItem.difficulty.toLowerCase() as any}>
                    {currentItem.difficulty}
                  </Badge>
                  <Badge color={currentItem.topicColor}>{currentItem.topicName}</Badge>
                  <span className="text-xs font-black text-amber-500 flex items-center gap-1">
                    <Star className="w-3.5 h-3.5 fill-current" /> {currentItem.xpReward} XP
                  </span>
                </div>

                <div className="text-[11px] font-semibold text-gray-400">
                  Repetition #{currentItem.reviewCount} · Ease {currentItem.easeFactor.toFixed(1)}
                </div>
              </div>

              {/* Title & Prompt */}
              <h2 className="text-2xl font-black text-gray-900 dark:text-white mb-3">
                {currentItem.taskTitle}
              </h2>

              {currentItem.taskDescription && (
                <p className="text-sm text-gray-600 dark:text-gray-300 leading-relaxed mb-6">
                  {currentItem.taskDescription}
                </p>
              )}

              {/* Problem link */}
              <div className="flex items-center gap-3 mb-8">
                <a
                  href={toSafeUrl(currentItem.platformLink)}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 text-xs font-bold hover:bg-blue-100 transition-colors"
                >
                  <span>Open Problem</span>
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>

                <button
                  type="button"
                  onClick={() => setNoteModalOpen(true)}
                  className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-gray-100 dark:bg-slate-800 text-gray-700 dark:text-gray-300 text-xs font-bold hover:bg-gray-200 transition-colors"
                >
                  <span>View Notes & Snippets</span>
                </button>
              </div>

              {/* Solution/Prompt Reveal Section */}
              {!revealed ? (
                <div className="text-center pt-6 border-t border-gray-100 dark:border-slate-800">
                  <p className="text-xs text-gray-500 dark:text-gray-400 mb-3">
                    Recall your solution, time complexity, and edge cases before revealing
                  </p>
                  <motion.button
                    whileHover={{ scale: 1.03 }}
                    whileTap={{ scale: 0.97 }}
                    onClick={() => setRevealed(true)}
                    className="px-6 py-3 rounded-2xl bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-black text-sm shadow-md shadow-blue-500/25 flex items-center gap-2 mx-auto"
                  >
                    <span>Reveal Answer & Rate Recall</span>
                    <span className="text-[10px] px-2 py-0.5 rounded-md bg-white/20 font-bold hidden sm:inline">Space</span>
                  </motion.button>
                </div>
              ) : (
                <div className="space-y-4 pt-6 border-t border-gray-100 dark:border-slate-800 animate-fade-in">
                  <div className="text-center">
                    <span className="text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400">
                      How easily did you recall this solution?
                    </span>
                  </div>

                  {/* 5 Rating Buttons (SM-2 standard) */}
                  <div className="grid grid-cols-2 sm:grid-cols-5 gap-2.5">
                    {/* 1 - Again */}
                    <button
                      onClick={() => handleRate(1)}
                      disabled={submitting}
                      className="p-3 rounded-xl border border-rose-200 dark:border-rose-900/60 bg-rose-50/50 dark:bg-rose-950/20 hover:bg-rose-100 text-center transition-all group active:scale-95"
                    >
                      <div className="text-xs font-black text-rose-600 dark:text-rose-400">1 · Again</div>
                      <div className="text-[10px] text-gray-500 dark:text-gray-400 mt-0.5">Blackout (1d)</div>
                    </button>

                    {/* 2 - Hard */}
                    <button
                      onClick={() => handleRate(2)}
                      disabled={submitting}
                      className="p-3 rounded-xl border border-orange-200 dark:border-orange-900/60 bg-orange-50/50 dark:bg-orange-950/20 hover:bg-orange-100 text-center transition-all group active:scale-95"
                    >
                      <div className="text-xs font-black text-orange-600 dark:text-orange-400">2 · Hard</div>
                      <div className="text-[10px] text-gray-500 dark:text-gray-400 mt-0.5">Struggled (1d)</div>
                    </button>

                    {/* 3 - Good */}
                    <button
                      onClick={() => handleRate(3)}
                      disabled={submitting}
                      className="p-3 rounded-xl border border-blue-200 dark:border-blue-900/60 bg-blue-50/50 dark:bg-blue-950/20 hover:bg-blue-100 text-center transition-all group active:scale-95"
                    >
                      <div className="text-xs font-black text-blue-600 dark:text-blue-400">3 · Good</div>
                      <div className="text-[10px] text-gray-500 dark:text-gray-400 mt-0.5">Solid Recall</div>
                    </button>

                    {/* 4 - Easy */}
                    <button
                      onClick={() => handleRate(4)}
                      disabled={submitting}
                      className="p-3 rounded-xl border border-teal-200 dark:border-teal-900/60 bg-teal-50/50 dark:bg-teal-950/20 hover:bg-teal-100 text-center transition-all group active:scale-95"
                    >
                      <div className="text-xs font-black text-teal-600 dark:text-teal-400">4 · Easy</div>
                      <div className="text-[10px] text-gray-500 dark:text-gray-400 mt-0.5">Quick Recall</div>
                    </button>

                    {/* 5 - Mastered */}
                    <button
                      onClick={() => handleRate(5)}
                      disabled={submitting}
                      className="p-3 rounded-xl border border-emerald-200 dark:border-emerald-900/60 bg-emerald-50/50 dark:bg-emerald-950/20 hover:bg-emerald-100 text-center transition-all group active:scale-95 col-span-2 sm:col-span-1"
                    >
                      <div className="text-xs font-black text-emerald-600 dark:text-emerald-400">5 · Mastered</div>
                      <div className="text-[10px] text-gray-500 dark:text-gray-400 mt-0.5">Instant!</div>
                    </button>
                  </div>
                </div>
              )}
            </Card>
          </motion.div>
        </div>
      )}

      {/* Note modal for inspecting saved solution */}
      {currentItem && (
        <NoteModal
          taskId={currentItem.taskId}
          taskTitle={currentItem.taskTitle}
          isOpen={noteModalOpen}
          onClose={() => setNoteModalOpen(false)}
        />
      )}
    </div>
  );
};

export default ReviewPage;
