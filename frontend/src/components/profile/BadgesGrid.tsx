import React, { useEffect, useState } from 'react';
import {
  Flame, Zap, Shield, Award, Crown, Target, CheckCircle2,
  Sparkles, Medal, Trophy, Star, Gem, Timer, Moon,
  TrendingUp, Lock, Check
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { api } from '../../lib/api';
import { Badge } from '../../types';

interface BadgesGridProps {
  userId?: number;
}

const ICON_MAP: Record<string, React.ReactNode> = {
  Flame: <Flame className="w-6 h-6" />,
  Zap: <Zap className="w-6 h-6" />,
  Shield: <Shield className="w-6 h-6" />,
  Award: <Award className="w-6 h-6" />,
  Crown: <Crown className="w-6 h-6" />,
  Target: <Target className="w-6 h-6" />,
  CheckCircle2: <CheckCircle2 className="w-6 h-6" />,
  Sparkles: <Sparkles className="w-6 h-6" />,
  Medal: <Medal className="w-6 h-6" />,
  Trophy: <Trophy className="w-6 h-6" />,
  Star: <Star className="w-6 h-6" />,
  Gem: <Gem className="w-6 h-6" />,
  Timer: <Timer className="w-6 h-6" />,
  Moon: <Moon className="w-6 h-6" />,
  StarHalf: <Star className="w-6 h-6 fill-current" />,
  TrendingUp: <TrendingUp className="w-6 h-6" />,
};

const RARITY_STYLES: Record<string, { bg: string; text: string; border: string; glow: string; badgeTag: string }> = {
  COMMON: {
    bg: 'bg-slate-100 dark:bg-slate-800/80 text-slate-700 dark:text-slate-300',
    text: 'text-slate-600 dark:text-slate-400',
    border: 'border-slate-200 dark:border-slate-700',
    glow: '',
    badgeTag: 'bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-300',
  },
  RARE: {
    bg: 'bg-blue-50/80 dark:bg-blue-950/30 text-blue-600 dark:text-blue-400',
    text: 'text-blue-600 dark:text-blue-400',
    border: 'border-blue-200 dark:border-blue-800/60',
    glow: 'shadow-blue-500/10 dark:shadow-blue-500/20',
    badgeTag: 'bg-blue-100 dark:bg-blue-900/60 text-blue-700 dark:text-blue-300',
  },
  EPIC: {
    bg: 'bg-purple-50/80 dark:bg-purple-950/30 text-purple-600 dark:text-purple-400',
    text: 'text-purple-600 dark:text-purple-400',
    border: 'border-purple-200 dark:border-purple-800/60',
    glow: 'shadow-purple-500/15 dark:shadow-purple-500/25',
    badgeTag: 'bg-purple-100 dark:bg-purple-900/60 text-purple-700 dark:text-purple-300',
  },
  LEGENDARY: {
    bg: 'bg-gradient-to-br from-amber-500/10 to-orange-500/10 text-amber-600 dark:text-amber-400',
    text: 'text-amber-600 dark:text-amber-400',
    border: 'border-amber-300 dark:border-amber-700/80',
    glow: 'shadow-amber-500/20 dark:shadow-amber-500/30 shadow-lg',
    badgeTag: 'bg-gradient-to-r from-amber-500 to-orange-500 text-white font-bold',
  },
};

export const BadgesGrid: React.FC<BadgesGridProps> = ({ userId }) => {
  const [badges, setBadges] = useState<Badge[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [selectedBadge, setSelectedBadge] = useState<Badge | null>(null);

  useEffect(() => {
    setLoading(true);
    api.getBadges()
      .then((data) => setBadges(data))
      .catch((err) => console.error('Failed to load badges', err))
      .finally(() => setLoading(false));
  }, [userId]);

  const categories = ['ALL', 'STREAK', 'PROBLEMS', 'XP', 'SPECIAL'];

  const filteredBadges = badges.filter((b) => {
    if (selectedCategory === 'ALL') return true;
    return b.category === selectedCategory;
  });

  const earnedCount = badges.filter((b) => b.earned).length;

  if (loading) {
    return (
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 animate-pulse">
        {[...Array(8)].map((_, i) => (
          <div key={i} className="h-36 rounded-2xl bg-gray-100 dark:bg-slate-800/60" />
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Category Tabs & Stats */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all whitespace-nowrap ${
                selectedCategory === cat
                  ? 'bg-blue-600 text-white shadow-md shadow-blue-500/20'
                  : 'bg-gray-100 dark:bg-slate-800 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-slate-700'
              }`}
            >
              {cat === 'ALL' ? 'All Badges' : cat.charAt(0) + cat.slice(1).toLowerCase()}
            </button>
          ))}
        </div>

        <div className="text-xs font-bold text-gray-500 dark:text-gray-400 flex items-center gap-2">
          <span>Unlocked:</span>
          <span className="text-blue-600 dark:text-blue-400 font-extrabold text-sm">
            {earnedCount} / {badges.length}
          </span>
        </div>
      </div>

      {/* Badges Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3.5">
        {filteredBadges.map((badge, idx) => {
          const rarity = RARITY_STYLES[badge.rarity] || RARITY_STYLES.COMMON;
          return (
            <motion.div
              key={badge.id}
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.25, delay: idx * 0.03 }}
              whileHover={{ y: -3, scale: 1.02 }}
              onClick={() => setSelectedBadge(badge)}
              className={`cursor-pointer relative p-4 rounded-2xl border transition-all ${rarity.border} ${
                badge.earned
                  ? `${rarity.bg} ${rarity.glow} shadow-sm`
                  : 'bg-gray-50 dark:bg-slate-900/40 opacity-60 border-dashed border-gray-300 dark:border-slate-800'
              }`}
            >
              {/* Rarity & Status Corner */}
              <div className="flex items-center justify-between mb-2">
                <span className={`text-[10px] font-black uppercase px-2 py-0.5 rounded-md ${rarity.badgeTag}`}>
                  {badge.rarity}
                </span>
                {badge.earned ? (
                  <span className="w-4 h-4 rounded-full bg-emerald-500 text-white flex items-center justify-center">
                    <Check className="w-2.5 h-2.5 stroke-[3]" />
                  </span>
                ) : (
                  <Lock className="w-3.5 h-3.5 text-gray-400" />
                )}
              </div>

              {/* Icon */}
              <div className="flex justify-center my-2">
                <div
                  className={`w-12 h-12 rounded-2xl flex items-center justify-center transition-transform ${
                    badge.earned
                      ? `${rarity.text} bg-white dark:bg-slate-800 shadow-md`
                      : 'text-gray-400 bg-gray-200 dark:bg-slate-800'
                  }`}
                >
                  {ICON_MAP[badge.icon] || <Award className="w-6 h-6" />}
                </div>
              </div>

              {/* Title & Description */}
              <div className="text-center">
                <h4 className="text-xs font-bold text-gray-900 dark:text-white truncate">
                  {badge.name}
                </h4>
                <p className="text-[11px] text-gray-500 dark:text-gray-400 line-clamp-1 mt-0.5">
                  {badge.description}
                </p>
              </div>

              {/* Progress Bar for Locked */}
              {!badge.earned && (
                <div className="mt-3">
                  <div className="w-full bg-gray-200 dark:bg-slate-700/60 rounded-full h-1.5 overflow-hidden">
                    <div
                      className="bg-blue-500 h-full rounded-full transition-all duration-300"
                      style={{ width: `${badge.progressPercent}%` }}
                    />
                  </div>
                  <p className="text-[9px] text-right text-gray-400 font-semibold mt-1">
                    {badge.progressPercent}%
                  </p>
                </div>
              )}

              {/* Earned Date or XP Bonus */}
              {badge.earned && (
                <div className="mt-2 text-center">
                  <span className="text-[10px] font-bold text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/40 px-2 py-0.5 rounded-md">
                    +{badge.xpReward} XP
                  </span>
                </div>
              )}
            </motion.div>
          );
        })}
      </div>

      {/* Badge Detail Modal */}
      <AnimatePresence>
        {selectedBadge && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              className="bg-white dark:bg-slate-900 rounded-3xl p-6 max-w-sm w-full border border-gray-100 dark:border-slate-800 shadow-2xl text-center space-y-4 relative"
            >
              <button
                onClick={() => setSelectedBadge(null)}
                className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 text-sm font-bold"
              >
                ✕
              </button>

              <div className="flex justify-center">
                <div
                  className={`w-16 h-16 rounded-3xl flex items-center justify-center shadow-lg ${
                    selectedBadge.earned
                      ? 'bg-gradient-to-tr from-blue-600 to-indigo-600 text-white'
                      : 'bg-gray-100 dark:bg-slate-800 text-gray-400'
                  }`}
                >
                  {ICON_MAP[selectedBadge.icon] || <Award className="w-8 h-8" />}
                </div>
              </div>

              <div>
                <span className="text-[11px] font-black uppercase tracking-wider px-2.5 py-0.5 rounded-full bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300">
                  {selectedBadge.rarity} • {selectedBadge.category}
                </span>
                <h3 className="text-lg font-black text-gray-900 dark:text-white mt-2">
                  {selectedBadge.name}
                </h3>
                <p className="text-xs text-gray-600 dark:text-gray-300 mt-1">
                  {selectedBadge.description}
                </p>
              </div>

              <div className="p-3 bg-gray-50 dark:bg-slate-800/60 rounded-2xl flex items-center justify-around text-xs">
                <div>
                  <span className="text-gray-400 block text-[10px]">Reward</span>
                  <span className="font-extrabold text-amber-500">+{selectedBadge.xpReward} XP</span>
                </div>
                <div className="w-px h-6 bg-gray-200 dark:bg-slate-700" />
                <div>
                  <span className="text-gray-400 block text-[10px]">Status</span>
                  <span className={`font-extrabold ${selectedBadge.earned ? 'text-emerald-500' : 'text-gray-500'}`}>
                    {selectedBadge.earned ? 'Unlocked ✓' : `${selectedBadge.progressPercent}% Done`}
                  </span>
                </div>
              </div>

              {selectedBadge.earned && selectedBadge.earnedAt && (
                <p className="text-[11px] text-gray-400">
                  Achieved on {new Date(selectedBadge.earnedAt).toLocaleDateString()}
                </p>
              )}

              <button
                onClick={() => setSelectedBadge(null)}
                className="w-full py-2.5 rounded-xl bg-blue-600 text-white font-bold text-xs shadow-md shadow-blue-500/20 active:scale-98 transition-all"
              >
                Close
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default BadgesGrid;
