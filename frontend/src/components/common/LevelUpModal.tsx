import React, { useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronUp, Zap, Sparkles, X } from 'lucide-react';
import { triggerCelebrationConfetti } from '../../lib/confetti';

interface LevelUpModalProps {
  level: number;
  title: string;
  isOpen: boolean;
  onClose: () => void;
}

export const LevelUpModal: React.FC<LevelUpModalProps> = ({
  level,
  title,
  isOpen,
  onClose,
}) => {
  useEffect(() => {
    if (isOpen) {
      triggerCelebrationConfetti();
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
        <motion.div
          initial={{ scale: 0.8, opacity: 0, y: 30 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          exit={{ scale: 0.8, opacity: 0, y: 30 }}
          className="bg-gradient-to-b from-white to-gray-50 dark:from-slate-900 dark:to-slate-950 rounded-3xl p-6 max-w-sm w-full border border-blue-400/40 dark:border-blue-500/30 shadow-2xl text-center space-y-4 relative overflow-hidden"
        >
          {/* Ambient Cosmic Background */}
          <div className="absolute -top-24 left-1/2 -translate-x-1/2 w-48 h-48 bg-blue-500/20 rounded-full blur-3xl pointer-events-none" />

          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
          >
            <X className="w-5 h-5" />
          </button>

          <div className="flex justify-center">
            <div className="w-18 h-18 rounded-3xl bg-gradient-to-tr from-blue-600 to-indigo-600 text-white flex flex-col items-center justify-center shadow-lg shadow-blue-500/30">
              <ChevronUp className="w-6 h-6 stroke-[3] -mb-1 animate-pulse" />
              <span className="text-xl font-black">{level}</span>
            </div>
          </div>

          <div>
            <div className="flex items-center justify-center gap-1 text-blue-600 dark:text-blue-400 font-black text-xs uppercase tracking-wider">
              <Sparkles className="w-4 h-4" />
              <span>Level Up!</span>
              <Sparkles className="w-4 h-4" />
            </div>

            <h2 className="text-2xl font-black text-gray-900 dark:text-white mt-1">
              Level {level}
            </h2>
            <div className="mt-2 inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 dark:bg-blue-950/50 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800 text-xs font-black">
              <Zap className="w-3.5 h-3.5 fill-current" />
              <span>Title Unlocked: {title}</span>
            </div>

            <p className="text-xs text-gray-500 dark:text-gray-400 mt-3">
              Congratulations! Your problem-solving skills have advanced. Keep crushing daily problems!
            </p>
          </div>

          <button
            onClick={onClose}
            className="w-full py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-extrabold text-xs shadow-md shadow-blue-500/25 active:scale-95 transition-all"
          >
            Awesome!
          </button>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};

export default LevelUpModal;
