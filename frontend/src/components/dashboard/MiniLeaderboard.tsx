import React, { useEffect, useState } from 'react';
import { api } from '../../lib/api';
import { LeaderboardEntry } from '../../types';
import { useAuth } from '../../hooks/useAuth';
import Card from '../ui/Card';
import { Trophy } from 'lucide-react';
import { Link } from 'react-router-dom';
import LoadingSpinner from '../ui/LoadingSpinner';

const MiniLeaderboard: React.FC = () => {
  const [leaders, setLeaders] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    const fetchLeaders = async () => {
      try {
        const data = await api.getLeaderboard('week');
        setLeaders(data.slice(0, 5));
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchLeaders();
  }, []);

  const getRankIcon = (rank: number) => {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return <span className="text-sm font-bold text-gray-400">{rank}</span>;
  };

  return (
    <Card className="flex flex-col h-full">
      <div className="p-4 border-b border-gray-100 dark:border-slate-700/50 flex items-center gap-2">
        <Trophy className="w-5 h-5 text-amber-500" />
        <h3 className="font-bold dark:text-white">Top 5 This Week</h3>
      </div>
      
      <div className="flex-1 p-0">
        {loading ? (
          <div className="py-8"><LoadingSpinner size="sm" /></div>
        ) : (
          <div className="divide-y divide-gray-100 dark:divide-slate-700/50">
            {leaders.map((entry) => {
              const isMe = entry.userId === user?.id;
              return (
                <div 
                  key={entry.userId} 
                  className={`flex items-center gap-3 p-3 transition-colors ${
                    isMe ? 'bg-blue-50/50 dark:bg-blue-900/20' : 'hover:bg-gray-50 dark:hover:bg-slate-800'
                  }`}
                >
                  <div className="w-6 text-center">{getRankIcon(entry.rank)}</div>
                  
                  <img 
                    src={entry.userAvatar || `https://ui-avatars.com/api/?name=${entry.userName}`} 
                    alt={entry.userName} 
                    className="w-8 h-8 rounded-full border border-gray-200 dark:border-slate-600"
                  />
                  
                  <div className="flex-1 min-w-0">
                    <p className={`text-sm font-medium truncate ${isMe ? 'text-blue-700 dark:text-blue-400' : 'text-gray-900 dark:text-white'}`}>
                      {entry.userName}
                    </p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      {entry.tasksCompleted} solved
                    </p>
                  </div>
                  
                  <div className="text-right">
                    <p className="text-sm font-bold text-amber-500">{entry.totalXp} XP</p>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
      
      <div className="p-3 border-t border-gray-100 dark:border-slate-700/50 text-center">
        <Link 
          to="/leaderboard" 
          className="text-sm text-blue-600 dark:text-blue-400 hover:underline font-medium"
        >
          View Full Leaderboard
        </Link>
      </div>
    </Card>
  );
};

export default MiniLeaderboard;
