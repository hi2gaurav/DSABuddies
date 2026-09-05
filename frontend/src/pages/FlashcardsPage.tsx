import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  ChevronRight, ChevronLeft, Brain
} from 'lucide-react';
import { api } from '../lib/api';
import { Flashcard } from '../types';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import CoinRewardOverlay from '../components/common/CoinRewardOverlay';
import { useToast } from '../components/ui/Toast';
import { triggerCelebrationConfetti } from '../lib/confetti';

export const FlashcardsPage: React.FC = () => {
  const [flashcards, setFlashcards] = useState<Flashcard[]>([]);
  const [activeCategory, setActiveCategory] = useState<string>('ALL');
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [loading, setLoading] = useState(true);
  const [rewardXp, setRewardXp] = useState<number | null>(null);

  const { show } = useToast();

  useEffect(() => {
    fetchCards(activeCategory);
  }, [activeCategory]);

  const fetchCards = async (cat: string) => {
    setLoading(true);
    try {
      let cards: Flashcard[] = [];
      if (cat === 'DUE') {
        cards = await api.getDueFlashcards();
      } else {
        cards = await api.getFlashcards(cat === 'ALL' ? undefined : cat);
      }
      setFlashcards(cards);
      setCurrentIndex(0);
      setIsFlipped(false);
    } catch (_) {
      show('Failed to load flashcards', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Keyboard navigation: Space to flip, 1-5 to rate
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.code === 'Space') {
        e.preventDefault();
        setIsFlipped((prev) => !prev);
      } else if (isFlipped && ['1', '2', '3', '4', '5'].includes(e.key)) {
        handleRating(parseInt(e.key, 10));
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isFlipped, currentIndex, flashcards]);

  const handleRating = async (rating: number) => {
    const currentCard = flashcards[currentIndex];
    if (!currentCard) return;

    try {
      await api.submitFlashcardReview(currentCard.id, rating);
      setRewardXp(10);
      show(`Flashcard reviewed! +10 XP earned ⭐`, 'success');

      if (rating >= 4) {
        triggerCelebrationConfetti();
      }

      setIsFlipped(false);
      if (currentIndex < flashcards.length - 1) {
        setCurrentIndex((prev) => prev + 1);
      } else {
        show('All flashcards in this category reviewed! 🎉', 'success');
        fetchCards(activeCategory);
      }
    } catch (_) {
      show('Failed to submit review', 'error');
    }
  };

  const categories = [
    { id: 'ALL', label: 'All Topics' },
    { id: 'DUE', label: 'Due for Review' },
    { id: 'JAVA', label: 'Java Core & JVM' },
    { id: 'SPRING_BOOT', label: 'Spring Boot' },
    { id: 'DATABASE', label: 'Databases & SQL' },
    { id: 'CS_FUNDAMENTALS', label: 'CS Fundamentals' },
  ];

  const currentCard: Flashcard | undefined = flashcards[currentIndex];
  const masteredCount = flashcards.filter((c) => c.intervalDays >= 7).length;

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Coin Reward Overlay */}
      {rewardXp !== null && (
        <CoinRewardOverlay
          xp={rewardXp}
          onComplete={() => setRewardXp(null)}
        />
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black text-gray-900 dark:text-white flex items-center gap-2.5">
            <span>Spaced Repetition Flashcards</span>
            <Brain className="w-8 h-8 text-blue-500" />
          </h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            SuperMemo SM-2 flashcards engineered for long-term retention of technical interview concepts.
          </p>
        </div>

        {flashcards.length > 0 && (
          <div className="text-xs font-bold text-gray-500 dark:text-gray-400 flex items-center gap-3">
            <span>Card {currentIndex + 1} of {flashcards.length}</span>
            <span className="w-px h-4 bg-gray-200 dark:bg-slate-700" />
            <span className="text-emerald-600 dark:text-emerald-400 font-extrabold">
              {masteredCount} Mastered
            </span>
          </div>
        )}
      </div>

      {/* Category Tabs */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1">
        {categories.map((cat) => (
          <button
            key={cat.id}
            onClick={() => setActiveCategory(cat.id)}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all whitespace-nowrap ${
              activeCategory === cat.id
                ? 'bg-blue-600 text-white shadow-md shadow-blue-500/20'
                : 'bg-gray-100 dark:bg-slate-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-slate-700'
            }`}
          >
            {cat.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="py-24"><LoadingSpinner size="lg" /></div>
      ) : flashcards.length === 0 ? (
        <Card className="p-16 text-center text-gray-500 dark:text-gray-400 space-y-3">
          <Brain className="w-12 h-12 mx-auto text-gray-400 opacity-60" />
          <h3 className="text-base font-bold text-gray-900 dark:text-white">No flashcards due!</h3>
          <p className="text-xs max-w-sm mx-auto">
            You're all caught up on this category. Switch categories above or check back tomorrow for your scheduled SM-2 reviews.
          </p>
        </Card>
      ) : currentCard && (
        <div className="space-y-6">
          {/* Flip Card Container */}
          <div
            className="perspective cursor-pointer select-none"
            onClick={() => setIsFlipped(!isFlipped)}
          >
            <motion.div
              initial={false}
              animate={{ rotateY: isFlipped ? 180 : 0 }}
              transition={{ duration: 0.5, ease: 'easeOut' }}
              className="relative w-full min-h-[340px] rounded-3xl preserve-3d shadow-xl"
            >
              {/* FRONT SIDE (Question) */}
              <div
                className={`absolute inset-0 p-8 rounded-3xl bg-white dark:bg-slate-900 border-2 border-blue-100 dark:border-slate-800 backface-hidden flex flex-col justify-between ${
                  isFlipped ? 'pointer-events-none' : ''
                }`}
              >
                <div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-black uppercase px-2.5 py-1 rounded-md bg-blue-50 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400">
                        {currentCard.category}
                      </span>
                      {currentCard.topic && (
                        <span className="text-[10px] font-bold text-gray-400">
                          {currentCard.topic}
                        </span>
                      )}
                    </div>
                    <Badge variant={currentCard.difficulty.toLowerCase() as any}>{currentCard.difficulty}</Badge>
                  </div>

                  <div className="my-10 text-center">
                    <h2 className="text-xl sm:text-2xl font-black text-gray-900 dark:text-white leading-snug">
                      {currentCard.question}
                    </h2>
                  </div>
                </div>

                <div className="text-center text-xs text-gray-400 font-semibold flex items-center justify-center gap-2">
                  <span>Click card or press</span>
                  <kbd className="px-2 py-0.5 bg-gray-100 dark:bg-slate-800 rounded-md border border-gray-200 dark:border-slate-700 text-gray-600 dark:text-gray-300 font-mono text-[10px]">
                    Space
                  </kbd>
                  <span>to reveal answer</span>
                </div>
              </div>

              {/* BACK SIDE (Answer & Code Snippet) */}
              <div
                className={`absolute inset-0 p-8 rounded-3xl bg-slate-950 text-white border-2 border-indigo-500/40 rotate-y-180 backface-hidden flex flex-col justify-between ${
                  !isFlipped ? 'pointer-events-none' : ''
                }`}
              >
                <div className="space-y-4 overflow-y-auto max-h-[250px] pr-2">
                  <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                    <span className="text-xs font-black text-blue-400 uppercase tracking-wider">
                      Reference Explanation
                    </span>
                    <span className="text-xs font-bold text-amber-400">
                      Interval: {currentCard.intervalDays}d • Ease: {currentCard.easeFactor}
                    </span>
                  </div>

                  <p className="text-xs sm:text-sm text-slate-200 whitespace-pre-wrap leading-relaxed">
                    {currentCard.answer}
                  </p>

                  {currentCard.codeSnippet && (
                    <div className="p-3 bg-slate-900 rounded-xl font-mono text-xs text-emerald-400 overflow-x-auto border border-slate-800">
                      <pre>{currentCard.codeSnippet}</pre>
                    </div>
                  )}
                </div>

                <div className="text-center text-xs text-slate-400 pt-3 border-t border-slate-800 flex items-center justify-center gap-1.5">
                  <span>Rate your recall below to schedule next review</span>
                </div>
              </div>
            </motion.div>
          </div>

          {/* SM-2 SuperMemo Rating Buttons (shown when flipped) */}
          <AnimatePresence>
            {isFlipped && (
              <motion.div
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 15 }}
                className="p-5 bg-white dark:bg-slate-900 rounded-3xl border border-gray-200 dark:border-slate-800 shadow-lg space-y-3"
              >
                <div className="flex items-center justify-between text-xs font-bold text-gray-500 dark:text-gray-400">
                  <span>How well did you recall this concept?</span>
                  <span className="text-[11px] text-gray-400">Keys 1 to 5</span>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-5 gap-2">
                  <button
                    onClick={() => handleRating(1)}
                    className="p-3 rounded-2xl bg-red-50 hover:bg-red-100 dark:bg-red-950/40 dark:hover:bg-red-950/60 border border-red-200 dark:border-red-900/40 text-center transition-transform active:scale-95"
                  >
                    <div className="text-sm font-black text-red-600 dark:text-red-400">1 • Again</div>
                    <div className="text-[10px] text-gray-500 mt-0.5">Reset (1 day)</div>
                  </button>

                  <button
                    onClick={() => handleRating(2)}
                    className="p-3 rounded-2xl bg-orange-50 hover:bg-orange-100 dark:bg-orange-950/40 dark:hover:bg-orange-950/60 border border-orange-200 dark:border-orange-900/40 text-center transition-transform active:scale-95"
                  >
                    <div className="text-sm font-black text-orange-600 dark:text-orange-400">2 • Hard</div>
                    <div className="text-[10px] text-gray-500 mt-0.5">Short interval</div>
                  </button>

                  <button
                    onClick={() => handleRating(3)}
                    className="p-3 rounded-2xl bg-yellow-50 hover:bg-yellow-100 dark:bg-yellow-950/40 dark:hover:bg-yellow-950/60 border border-yellow-200 dark:border-yellow-900/40 text-center transition-transform active:scale-95"
                  >
                    <div className="text-sm font-black text-yellow-600 dark:text-yellow-400">3 • Good</div>
                    <div className="text-[10px] text-gray-500 mt-0.5">Normal growth</div>
                  </button>

                  <button
                    onClick={() => handleRating(4)}
                    className="p-3 rounded-2xl bg-blue-50 hover:bg-blue-100 dark:bg-blue-950/40 dark:hover:bg-blue-950/60 border border-blue-200 dark:border-blue-900/40 text-center transition-transform active:scale-95"
                  >
                    <div className="text-sm font-black text-blue-600 dark:text-blue-400">4 • Easy</div>
                    <div className="text-[10px] text-gray-500 mt-0.5">Fast advance</div>
                  </button>

                  <button
                    onClick={() => handleRating(5)}
                    className="p-3 rounded-2xl bg-emerald-50 hover:bg-emerald-100 dark:bg-emerald-950/40 dark:hover:bg-emerald-950/60 border border-emerald-200 dark:border-emerald-900/40 text-center transition-transform active:scale-95 col-span-2 sm:col-span-1"
                  >
                    <div className="text-sm font-black text-emerald-600 dark:text-emerald-400">5 • Mastered</div>
                    <div className="text-[10px] text-gray-500 mt-0.5">Max interval</div>
                  </button>
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Navigation Controls */}
          <div className="flex items-center justify-between text-xs font-bold text-gray-500">
            <button
              disabled={currentIndex === 0}
              onClick={() => {
                setCurrentIndex((prev) => prev - 1);
                setIsFlipped(false);
              }}
              className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-gray-100 dark:bg-slate-800 disabled:opacity-40 hover:bg-gray-200 transition-colors"
            >
              <ChevronLeft className="w-4 h-4" /> Previous
            </button>

            <button
              disabled={currentIndex === flashcards.length - 1}
              onClick={() => {
                setCurrentIndex((prev) => prev + 1);
                setIsFlipped(false);
              }}
              className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-gray-100 dark:bg-slate-800 disabled:opacity-40 hover:bg-gray-200 transition-colors"
            >
              Next <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default FlashcardsPage;
