import React, { useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Award, Sparkles, X } from 'lucide-react';
import { Badge } from '../../types';
import { triggerCelebrationConfetti } from '../../lib/confetti';

interface BadgeCelebrationModalProps {
  badges: Badge[];
  isOpen: boolean;
  onClose: () => void;
}

export const BadgeCelebrationModal: React.FC<BadgeCelebrationModalProps> = ({
  badges,
  isOpen,
  onClose,
}) => {
  useEffect(() => {
    if (isOpen && badges.length > 0) {
      triggerCelebrationConfetti();
    }
  }, [isOpen, badges]);

  if (!isOpen || badges.length === 0) return null;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
        <motion.div
          initial={{ scale: 0.8, opacity: 0, y: 20 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          exit={{ scale: 0.8, opacity: 0, y: 20 }}
          className="bg-gradient-to-b from-white to-gray-50 dark:from-slate-900 dark:to-slate-950 rounded-3xl p-6 max-w-sm w-full border border-amber-300/40 dark:border-amber-500/30 shadow-2xl text-center space-y-4 relative overflow-hidden"
        >
          {/* Glowing Aura Effect */}
          <div className="absolute -top-24 left-1/2 -translate-x-1/2 w-48 h-48 bg-amber-400/20 rounded-full blur-3xl pointer-events-none" />

          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
          >
            <X className="w-5 h-5" />
          </button>

          <div className="flex justify-center">
            <div className="w-16 h-16 rounded-full bg-gradient-to-tr from-amber-400 to-orange-500 text-white flex items-center justify-center shadow-lg shadow-amber-500/30 animate-bounce">
              <Award className="w-8 h-8" />
            </div>
          </div>

          <div>
            <div className="flex items-center justify-center gap-1.5 text-amber-500 font-black text-xs uppercase tracking-wider">
              <Sparkles className="w-4 h-4" />
              <span>Badge Unlocked!</span>
              <Sparkles className="w-4 h-4" />
            </div>

            {badges.map((badge) => (
              <div key={badge.id} className="mt-3 p-3 bg-amber-50/50 dark:bg-amber-950/20 rounded-2xl border border-amber-200/60 dark:border-amber-800/40">
                <h3 className="text-base font-black text-gray-900 dark:text-white">
                  {badge.name}
                </h3>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                  {badge.description}
                </p>
                <div className="mt-2 inline-block px-2.5 py-0.5 rounded-full text-xs font-black bg-amber-500 text-white">
                  +{badge.xpReward} XP Bonus
                </div>
              </div>
            ))}
          </div>

          <button
            onClick={onClose}
            className="w-full py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 text-white font-extrabold text-xs shadow-md shadow-amber-500/25 active:scale-95 transition-all"
          >
            Claim & Continue
          </button>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};

export default BadgeCelebrationModal;
