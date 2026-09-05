import React from 'react';
import Card from '../ui/Card';
import { Award, Flame, Zap, Trophy, Target, ShieldCheck } from 'lucide-react';
import { User } from '../../types';

interface AchievementsListProps {
  user: User;
  problemsSolved: number;
}

interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  unlocked: boolean;
  current: number;
  target: number;
  color: string;
}

export const AchievementsList: React.FC<AchievementsListProps> = ({ user, problemsSolved }) => {
  const achievements: Achievement[] = [
    {
      id: 'first_blood',
      title: 'First Step',
      description: 'Solve your first DSA problem',
      icon: <Target className="w-5 h-5 text-emerald-400" />,
      unlocked: problemsSolved >= 1,
      current: Math.min(problemsSolved, 1),
      target: 1,
      color: 'from-emerald-500/20 to-teal-500/10',
    },
    {
      id: 'streak_3',
      title: 'Habit Builder',
      description: 'Build a 3-day problem solving streak',
      icon: <Flame className="w-5 h-5 text-orange-400" />,
      unlocked: user.maxStreak >= 3,
      current: Math.min(user.maxStreak, 3),
      target: 3,
      color: 'from-orange-500/20 to-amber-500/10',
    },
    {
      id: 'xp_500',
      title: 'Century Club',
      description: 'Accumulate 500+ XP in your journey',
      icon: <Zap className="w-5 h-5 text-yellow-400" />,
      unlocked: user.totalXp >= 500,
      current: Math.min(user.totalXp, 500),
      target: 500,
      color: 'from-yellow-500/20 to-amber-500/10',
    },
    {
      id: 'solved_10',
      title: 'Decathlon',
      description: 'Complete 10 DSA challenges',
      icon: <Award className="w-5 h-5 text-blue-400" />,
      unlocked: problemsSolved >= 10,
      current: Math.min(problemsSolved, 10),
      target: 10,
      color: 'from-blue-500/20 to-indigo-500/10',
    },
    {
      id: 'xp_1500',
      title: 'DSA Grandmaster',
      description: 'Reach 1,500 total XP',
      icon: <Trophy className="w-5 h-5 text-purple-400" />,
      unlocked: user.totalXp >= 1500,
      current: Math.min(user.totalXp, 1500),
      target: 1500,
      color: 'from-purple-500/20 to-pink-500/10',
    },
  ];

  const unlockedCount = achievements.filter((a) => a.unlocked).length;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <Award className="w-5 h-5 text-amber-500" />
            <span>Milestone Achievements</span>
          </h3>
          <p className="text-xs text-gray-500 dark:text-gray-400">
            {unlockedCount} of {achievements.length} badges unlocked
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
        {achievements.map((item) => {
          const progressPercent = Math.min(100, Math.round((item.current / item.target) * 100));

          return (
            <Card
              key={item.id}
              className={`p-4 transition-all relative overflow-hidden ${
                item.unlocked
                  ? 'border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800'
                  : 'border-dashed border-gray-200 dark:border-slate-800 bg-gray-50/50 dark:bg-slate-900/40 opacity-75'
              }`}
            >
              <div className="flex items-start justify-between gap-2 mb-2">
                <div className={`p-2.5 rounded-xl bg-gradient-to-br ${item.color} border border-white/10 flex-shrink-0`}>
                  {item.icon}
                </div>
                {item.unlocked ? (
                  <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/40 px-2 py-0.5 rounded-full">
                    <ShieldCheck className="w-3 h-3" />
                    <span>Unlocked</span>
                  </span>
                ) : (
                  <span className="text-[11px] font-medium text-gray-400 dark:text-gray-500">
                    {item.current}/{item.target}
                  </span>
                )}
              </div>

              <h4 className="font-semibold text-gray-900 dark:text-white text-sm mb-0.5">{item.title}</h4>
              <p className="text-xs text-gray-500 dark:text-gray-400 mb-3">{item.description}</p>

              <div className="w-full bg-gray-100 dark:bg-slate-700 h-1.5 rounded-full overflow-hidden">
                <div
                  className={`h-full transition-all duration-500 ${
                    item.unlocked ? 'bg-emerald-500' : 'bg-blue-500'
                  }`}
                  style={{ width: `${progressPercent}%` }}
                />
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
};

export default AchievementsList;
