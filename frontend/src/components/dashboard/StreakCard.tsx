import React from 'react';
import Card from '../ui/Card';
import { Flame } from 'lucide-react';

interface StreakCardProps {
  currentStreak: number;
  maxStreak: number;
}

const StreakCard: React.FC<StreakCardProps> = ({ currentStreak, maxStreak }) => {
  return (
    <Card className="relative overflow-hidden bg-gradient-to-br from-orange-500 to-red-600 border-0 p-6 text-white shadow-lg shadow-orange-500/30">
      <div className="absolute top-0 right-0 -mt-4 -mr-4 text-white/10">
        <Flame className="w-32 h-32" />
      </div>
      
      <div className="relative z-10">
        <div className="flex items-center gap-2 mb-2">
          <div className="bg-white/20 p-2 rounded-lg backdrop-blur-sm animate-pulse">
            <span className="text-2xl">🔥</span>
          </div>
          <h3 className="text-lg font-medium text-orange-50">Current Streak</h3>
        </div>
        
        <div className="flex items-baseline gap-2 mt-4">
          <span className="text-5xl font-bold">{currentStreak}</span>
          <span className="text-xl text-orange-100 font-medium">days</span>
        </div>
        
        <div className="mt-4 pt-4 border-t border-white/20 flex justify-between items-center">
          <span className="text-sm text-orange-100">Max streak: {maxStreak} days</span>
          {currentStreak > 0 ? (
            <span className="text-xs bg-white text-orange-600 px-2 py-1 rounded-full font-bold">
              Keep it going!
            </span>
          ) : (
            <span className="text-xs bg-white text-orange-600 px-2 py-1 rounded-full font-bold">
              Start today!
            </span>
          )}
        </div>
      </div>
    </Card>
  );
};

export default StreakCard;
