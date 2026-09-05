import React, { useEffect, useState } from 'react';
import { Star, Sparkles } from 'lucide-react';
import { triggerCelebrationConfetti } from '../../lib/confetti';

interface CoinRewardOverlayProps {
  xp: number;
  onComplete?: () => void;
}

export const CoinRewardOverlay: React.FC<CoinRewardOverlayProps> = ({ xp, onComplete }) => {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    triggerCelebrationConfetti();
    const timer = setTimeout(() => {
      setVisible(false);
      onComplete?.();
    }, 2400);

    return () => clearTimeout(timer);
  }, [onComplete]);

  if (!visible) return null;

  return (
    <div className="fixed inset-0 z-50 pointer-events-none flex items-center justify-center">
      {/* Background radial flash */}
      <div className="absolute inset-0 bg-black/20 backdrop-blur-[1px] animate-fade-in transition-opacity" />

      {/* Floating Animated Coin Container */}
      <div className="relative flex flex-col items-center animate-coin-pop">
        {/* Glow halo */}
        <div className="absolute w-40 h-40 bg-amber-400/30 rounded-full blur-2xl animate-pulse" />

        {/* 3D Spinning Golden Coin */}
        <div className="relative w-24 h-24 rounded-full bg-gradient-to-tr from-amber-600 via-yellow-400 to-amber-200 p-1.5 shadow-2xl shadow-yellow-500/50 border-2 border-yellow-200 animate-coin-spin flex items-center justify-center">
          {/* Inner ring */}
          <div className="w-full h-full rounded-full border-2 border-dashed border-amber-800/40 flex items-center justify-center bg-gradient-to-br from-yellow-300 to-amber-500 shadow-inner">
            <span className="text-3xl font-extrabold text-amber-950 drop-shadow-sm select-none">
              ★
            </span>
          </div>

          {/* Sparkles around coin */}
          <Sparkles className="w-6 h-6 text-yellow-200 absolute -top-2 -right-2 animate-bounce" />
          <Star className="w-4 h-4 text-amber-100 fill-yellow-200 absolute -bottom-1 -left-2 animate-pulse" />
        </div>

        {/* XP Credit Badge */}
        <div className="mt-4 px-5 py-2 bg-gradient-to-r from-amber-500 via-yellow-500 to-amber-600 text-white font-extrabold text-lg sm:text-xl rounded-full shadow-lg shadow-amber-500/40 border border-yellow-200/50 flex items-center gap-2 animate-bounce">
          <Star className="w-5 h-5 fill-white text-yellow-100" />
          <span>+{xp} XP Credited!</span>
        </div>

        <p className="mt-1 text-xs font-semibold text-yellow-300 drop-shadow-md tracking-wider uppercase">
          Problem Solved!
        </p>
      </div>
    </div>
  );
};

export default CoinRewardOverlay;
