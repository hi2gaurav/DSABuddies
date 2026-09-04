import React, { useEffect, useState } from 'react';
import { api } from '../lib/api';
import { LeaderboardEntry } from '../types';
import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import Card from '../components/ui/Card';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { Trophy, Star, Flame } from 'lucide-react';
import { clsx } from 'clsx';

const LeaderboardPage: React.FC = () => {
  const [leaders, setLeaders] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState<'all' | 'week'>('all');
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchLeaders = async () => {
      setLoading(true);
      try {
        const data = await api.getLeaderboard(period === 'all' ? undefined : period);
        setLeaders(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchLeaders();
  }, [period]);

  const getRankBadge = (rank: number) => {
    if (rank === 1) return <div className="w-10 h-10 rounded-full bg-yellow-100 dark:bg-yellow-900/30 flex items-center justify-center text-2xl shadow-sm border border-yellow-200 dark:border-yellow-700/50">🥇</div>;
    if (rank === 2) return <div className="w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center text-2xl shadow-sm border border-gray-200 dark:border-gray-600">🥈</div>;
    if (rank === 3) return <div className="w-10 h-10 rounded-full bg-orange-50 dark:bg-orange-900/30 flex items-center justify-center text-2xl shadow-sm border border-orange-200 dark:border-orange-800/50">🥉</div>;
    return <div className="w-10 h-10 rounded-full bg-gray-50 dark:bg-slate-800 flex items-center justify-center font-bold text-gray-500 dark:text-gray-400">{rank}</div>;
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div className="flex flex-col sm:flex-row justify-between items-center gap-4 text-center sm:text-left">
        <div>
          <h1 className="text-3xl font-bold dark:text-white flex items-center justify-center sm:justify-start gap-3">
            Leaderboard <Trophy className="w-8 h-8 text-amber-500" />
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">See how you rank against your study group.</p>
        </div>
        
        <div className="bg-gray-100 dark:bg-slate-800 p-1 rounded-xl inline-flex">
          <button 
            className={clsx(
              "px-6 py-2 rounded-lg font-medium text-sm transition-all",
              period === 'all' 
                ? "bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-sm" 
                : "text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
            )}
            onClick={() => setPeriod('all')}
          >
            All Time
          </button>
          <button 
            className={clsx(
              "px-6 py-2 rounded-lg font-medium text-sm transition-all",
              period === 'week' 
                ? "bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-sm" 
                : "text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
            )}
            onClick={() => setPeriod('week')}
          >
            This Week
          </button>
        </div>
      </div>

      <Card className="overflow-hidden">
        {loading ? (
          <div className="py-20"><LoadingSpinner size="lg" /></div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse min-w-[600px]">
              <thead>
                <tr className="bg-gray-50 dark:bg-slate-800/80 border-b border-gray-200 dark:border-slate-700 text-gray-500 dark:text-gray-400 text-sm">
                  <th className="py-4 px-6 font-medium w-24 text-center">Rank</th>
                  <th className="py-4 px-6 font-medium">Buddy</th>
                  <th className="py-4 px-6 font-medium text-center">Tasks Solved</th>
                  <th className="py-4 px-6 font-medium text-center">Streak</th>
                  <th className="py-4 px-6 font-medium text-right">Total XP</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-slate-700/50">
                {leaders.map((entry) => {
                  const isMe = entry.userId === user?.id;
                  
                  return (
                    <tr 
                      key={entry.userId}
                      className={clsx(
                        "group transition-colors",
                        isMe ? "bg-blue-50/50 dark:bg-blue-900/20" : "hover:bg-gray-50 dark:hover:bg-slate-800/50"
                      )}
                    >
                      <td className="py-4 px-6">
                        <div className="flex justify-center">
                          {getRankBadge(entry.rank)}
                        </div>
                      </td>
                      <td className="py-4 px-6">
                        <div 
                          className="flex items-center gap-4 cursor-pointer"
                          onClick={() => navigate(`/profile/${entry.userId}`)}
                        >
                          <img 
                            src={entry.userAvatar || `https://ui-avatars.com/api/?name=${entry.userName}`} 
                            alt={entry.userName} 
                            className="w-12 h-12 rounded-full border-2 border-white dark:border-slate-700 shadow-sm"
                          />
                          <div>
                            <p className={clsx(
                              "font-bold text-base group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors",
                              isMe ? "text-blue-700 dark:text-blue-400" : "text-gray-900 dark:text-white"
                            )}>
                              {entry.userName} {isMe && <span className="text-xs font-normal bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300 px-2 py-0.5 rounded-full ml-2">You</span>}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="py-4 px-6 text-center">
                        <span className="font-medium text-gray-700 dark:text-gray-300">
                          {entry.tasksCompleted}
                        </span>
                      </td>
                      <td className="py-4 px-6 text-center">
                        <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-orange-50 dark:bg-orange-900/20 text-orange-600 dark:text-orange-400 rounded-full font-medium">
                          <Flame className="w-4 h-4" /> {entry.currentStreak}
                        </div>
                      </td>
                      <td className="py-4 px-6 text-right">
                        <div className="inline-flex items-center gap-1.5 font-bold text-lg text-amber-500">
                          <Star className="w-5 h-5 fill-current" /> {entry.totalXp}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
            
            {leaders.length === 0 && (
              <div className="py-12 text-center text-gray-500 dark:text-gray-400">
                No leaderboard data available for this period.
              </div>
            )}
          </div>
        )}
      </Card>
    </div>
  );
};

export default LeaderboardPage;
