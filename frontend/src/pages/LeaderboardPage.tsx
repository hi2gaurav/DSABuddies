import React, { useEffect, useState, useMemo } from 'react';
import { api } from '../lib/api';
import { LeaderboardEntry, LeaderboardSnapshot } from '../types';
import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import Card from '../components/ui/Card';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import AnimatedNumber from '../components/common/AnimatedNumber';
import { motion } from 'framer-motion';
import { Trophy, Star, Flame, Search, History, Zap, Calendar } from 'lucide-react';
import { clsx } from 'clsx';

const LeaderboardPage: React.FC = () => {
  const [leaders, setLeaders] = useState<LeaderboardEntry[]>([]);
  const [snapshots, setSnapshots] = useState<LeaderboardSnapshot[]>([]);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState<'all' | 'weekly' | 'monthly' | 'history'>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        if (period === 'history') {
          const historyData = await api.getLeaderboardHistory('weekly');
          setSnapshots(historyData);
        } else {
          const data = await api.getLeaderboard(period === 'all' ? undefined : period);
          setLeaders(data);
        }
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [period]);

  const filteredLeaders = useMemo(() => {
    return leaders.filter((l) =>
      l.userName.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [leaders, searchQuery]);

  const getRankBadge = (rank: number) => {
    if (rank === 1) return <div className="w-10 h-10 rounded-full bg-yellow-100 dark:bg-yellow-900/40 flex items-center justify-center text-xl shadow-sm border border-yellow-300 dark:border-yellow-700/60">🥇</div>;
    if (rank === 2) return <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-xl shadow-sm border border-slate-300 dark:border-slate-600">🥈</div>;
    if (rank === 3) return <div className="w-10 h-10 rounded-full bg-amber-50 dark:bg-amber-950/40 flex items-center justify-center text-xl shadow-sm border border-amber-300 dark:border-amber-800/60">🥉</div>;
    return <div className="w-10 h-10 rounded-full bg-gray-50 dark:bg-slate-800 flex items-center justify-center font-bold text-gray-500 dark:text-gray-400 text-sm border border-gray-100 dark:border-slate-700">{rank}</div>;
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold dark:text-white flex items-center gap-2.5">
            <span>Community Leaderboard</span>
            <Trophy className="w-7 h-7 text-amber-500" />
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1 text-sm">
            Celebrate streaks, weekly sprints, and group standings.
          </p>
        </div>
        
        {/* Period Tabs */}
        <div className="flex items-center gap-3 flex-wrap">
          <div className="bg-gray-100 dark:bg-slate-800 p-1 rounded-xl inline-flex text-xs font-medium flex-wrap">
            <button 
              className={clsx(
                "px-3.5 py-1.5 rounded-lg transition-all",
                period === 'weekly' 
                  ? "bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-xs font-bold" 
                  : "text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
              )}
              onClick={() => setPeriod('weekly')}
            >
              This Week
            </button>
            <button 
              className={clsx(
                "px-3.5 py-1.5 rounded-lg transition-all",
                period === 'monthly' 
                  ? "bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-xs font-bold" 
                  : "text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
              )}
              onClick={() => setPeriod('monthly')}
            >
              This Month
            </button>
            <button 
              className={clsx(
                "px-3.5 py-1.5 rounded-lg transition-all",
                period === 'all' 
                  ? "bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-xs font-bold" 
                  : "text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
              )}
              onClick={() => setPeriod('all')}
            >
              All Time
            </button>
            <button 
              className={clsx(
                "px-3.5 py-1.5 rounded-lg transition-all flex items-center gap-1",
                period === 'history' 
                  ? "bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-xs font-bold" 
                  : "text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
              )}
              onClick={() => setPeriod('history')}
            >
              <History className="w-3.5 h-3.5" />
              <span>Champions</span>
            </button>
          </div>
        </div>
      </div>

      {/* Member Search Bar (only for live rankings) */}
      {period !== 'history' && (
        <div className="relative max-w-sm">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search member by name..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-sm rounded-xl border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      )}

      {/* Top 3 Podium Display */}
      {period !== 'history' && !loading && !searchQuery && filteredLeaders.length >= 3 && (
        <div className="grid grid-cols-3 gap-3 sm:gap-6 pt-6 pb-2 items-end max-w-2xl mx-auto">
          {/* 2nd Place */}
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, delay: 0.1 }}
            className="flex flex-col items-center cursor-pointer group"
            onClick={() => navigate(`/profile/${filteredLeaders[1].userId}`)}
          >
            <div className="relative mb-2">
              <img 
                src={filteredLeaders[1].userAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(filteredLeaders[1].userName)}&background=64748b&color=fff`} 
                alt={filteredLeaders[1].userName} 
                className="w-14 h-14 sm:w-16 sm:h-16 rounded-full border-4 border-slate-300 dark:border-slate-600 object-cover shadow-lg group-hover:scale-105 transition-transform" 
              />
              <span className="absolute -bottom-2 left-1/2 -translate-x-1/2 text-xl">🥈</span>
            </div>
            <p className="font-bold text-xs sm:text-sm text-gray-900 dark:text-white truncate max-w-[100px] text-center mt-1">
              {filteredLeaders[1].userName.split(' ')[0]}
            </p>
            <div className="flex items-center gap-1 text-[10px] text-blue-600 dark:text-blue-400 font-bold">
              <span>Lv. {filteredLeaders[1].level || 1}</span>
            </div>
            <p className="text-xs font-black text-amber-500">
              <AnimatedNumber value={filteredLeaders[1].totalXp} suffix=" XP" />
            </p>
            <div className="w-full bg-slate-200/80 dark:bg-slate-800 rounded-t-2xl h-20 sm:h-24 mt-3 flex items-center justify-center font-black text-slate-400 text-lg border-t-2 border-slate-300 dark:border-slate-700">
              2
            </div>
          </motion.div>

          {/* 1st Place (Champion) */}
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
            className="flex flex-col items-center cursor-pointer group -mt-4"
            onClick={() => navigate(`/profile/${filteredLeaders[0].userId}`)}
          >
            <div className="relative mb-2">
              <div className="absolute -inset-1 bg-gradient-to-r from-amber-400 to-yellow-300 rounded-full blur-xs opacity-75 animate-pulse" />
              <img 
                src={filteredLeaders[0].userAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(filteredLeaders[0].userName)}&background=eab308&color=fff`} 
                alt={filteredLeaders[0].userName} 
                className="relative w-18 h-18 sm:w-20 sm:h-20 rounded-full border-4 border-yellow-400 object-cover shadow-xl group-hover:scale-105 transition-transform" 
              />
              <span className="absolute -bottom-2 left-1/2 -translate-x-1/2 text-2xl animate-bounce">👑</span>
            </div>
            <p className="font-black text-sm sm:text-base text-gray-900 dark:text-white truncate max-w-[110px] text-center mt-1">
              {filteredLeaders[0].userName.split(' ')[0]}
            </p>
            <div className="flex items-center gap-1 text-[10px] text-blue-600 dark:text-blue-400 font-bold">
              <span>Lv. {filteredLeaders[0].level || 1} • {filteredLeaders[0].title || 'Novice'}</span>
            </div>
            <p className="text-xs sm:text-sm font-black text-amber-500">
              <AnimatedNumber value={filteredLeaders[0].totalXp} suffix=" XP" />
            </p>
            <div className="w-full bg-gradient-to-b from-yellow-500/20 to-amber-500/10 dark:from-yellow-500/15 dark:to-slate-800 rounded-t-2xl h-28 sm:h-32 mt-3 flex items-center justify-center font-black text-yellow-600 dark:text-yellow-400 text-2xl border-t-4 border-yellow-400">
              1
            </div>
          </motion.div>

          {/* 3rd Place */}
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, delay: 0.2 }}
            className="flex flex-col items-center cursor-pointer group"
            onClick={() => navigate(`/profile/${filteredLeaders[2].userId}`)}
          >
            <div className="relative mb-2">
              <img 
                src={filteredLeaders[2].userAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(filteredLeaders[2].userName)}&background=d97706&color=fff`} 
                alt={filteredLeaders[2].userName} 
                className="w-14 h-14 sm:w-16 sm:h-16 rounded-full border-4 border-amber-600/70 object-cover shadow-lg group-hover:scale-105 transition-transform" 
              />
              <span className="absolute -bottom-2 left-1/2 -translate-x-1/2 text-xl">🥉</span>
            </div>
            <p className="font-bold text-xs sm:text-sm text-gray-900 dark:text-white truncate max-w-[100px] text-center mt-1">
              {filteredLeaders[2].userName.split(' ')[0]}
            </p>
            <div className="flex items-center gap-1 text-[10px] text-blue-600 dark:text-blue-400 font-bold">
              <span>Lv. {filteredLeaders[2].level || 1}</span>
            </div>
            <p className="text-xs font-black text-amber-500">
              <AnimatedNumber value={filteredLeaders[2].totalXp} suffix=" XP" />
            </p>
            <div className="w-full bg-amber-100/50 dark:bg-slate-800 rounded-t-2xl h-16 sm:h-20 mt-3 flex items-center justify-center font-black text-amber-700/60 dark:text-amber-500/60 text-lg border-t-2 border-amber-600/60">
              3
            </div>
          </motion.div>
        </div>
      )}

      {/* Main Content Area */}
      {period === 'history' ? (
        /* Hall of Fame / Previous Weekly Snapshots */
        <div className="space-y-4">
          <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <History className="w-5 h-5 text-amber-500" />
            Archived Weekly Champions
          </h3>

          {loading ? (
            <div className="py-20"><LoadingSpinner size="lg" /></div>
          ) : snapshots.length === 0 ? (
            <Card className="p-12 text-center text-gray-500 dark:text-gray-400">
              <History className="w-10 h-10 mx-auto mb-2 text-gray-400 opacity-50" />
              <p className="text-sm font-semibold">No archived snapshots yet.</p>
              <p className="text-xs mt-1">Standings are automatically archived every Monday midnight!</p>
            </Card>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {snapshots.map((snap) => {
                let parsed: LeaderboardEntry[] = [];
                try {
                  parsed = JSON.parse(snap.snapshotData || '[]');
                } catch (_) {}

                return (
                  <Card key={snap.id} className="p-5 border border-amber-200/60 dark:border-amber-900/40">
                    <div className="flex items-center justify-between border-b border-gray-100 dark:border-slate-800 pb-3 mb-3">
                      <div className="flex items-center gap-2 text-xs font-bold text-gray-500 dark:text-gray-400">
                        <Calendar className="w-4 h-4 text-amber-500" />
                        <span>{snap.periodStart} to {snap.periodEnd}</span>
                      </div>
                      <span className="text-[10px] font-black uppercase px-2 py-0.5 rounded-md bg-amber-100 dark:bg-amber-950/60 text-amber-700 dark:text-amber-300">
                        {snap.periodType}
                      </span>
                    </div>

                    <div className="space-y-2">
                      {parsed.slice(0, 5).map((entry, idx) => (
                        <div key={idx} className="flex items-center justify-between text-xs py-1">
                          <div className="flex items-center gap-2">
                            <span className="font-black text-amber-500 w-4">{idx + 1}.</span>
                            <span className="font-bold text-gray-900 dark:text-white">{entry.userName}</span>
                          </div>
                          <span className="font-black text-amber-500">+{entry.totalXp} XP</span>
                        </div>
                      ))}
                    </div>
                  </Card>
                );
              })}
            </div>
          )}
        </div>
      ) : (
        /* Leaderboard Table Card */
        <Card className="overflow-hidden border border-slate-200/80 dark:border-slate-800 shadow-lg">
          {loading ? (
            <div className="py-20"><LoadingSpinner size="lg" /></div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse min-w-[600px]">
                <thead>
                  <tr className="bg-gray-50/80 dark:bg-slate-800/80 border-b border-gray-200 dark:border-slate-700 text-gray-500 dark:text-gray-400 text-xs uppercase tracking-wider font-bold">
                    <th className="py-3.5 px-6 w-24 text-center">Rank</th>
                    <th className="py-3.5 px-6">Member</th>
                    <th className="py-3.5 px-6 text-center">Problems Solved</th>
                    <th className="py-3.5 px-6 text-center">Active Streak</th>
                    <th className="py-3.5 px-6 text-right">{period === 'all' ? 'Total XP' : 'XP Gained'}</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-slate-700/50">
                  {filteredLeaders.map((entry, idx) => {
                    const isMe = entry.userId === user?.id;
                    
                    return (
                      <motion.tr 
                        key={entry.userId}
                        initial={{ opacity: 0, y: 8 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.2, delay: idx * 0.025 }}
                        className={clsx(
                          "group transition-colors",
                          isMe ? "bg-blue-50/80 dark:bg-blue-900/30 font-semibold" : "hover:bg-gray-50/80 dark:hover:bg-slate-800/50"
                        )}
                      >
                        <td className="py-4 px-6">
                          <div className="flex justify-center">
                            {getRankBadge(entry.rank)}
                          </div>
                        </td>
                        <td className="py-4 px-6">
                          <div 
                            className="flex items-center gap-3.5 cursor-pointer"
                            onClick={() => navigate(`/profile/${entry.userId}`)}
                          >
                            <img 
                              src={entry.userAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(entry.userName)}`} 
                              alt={entry.userName} 
                              className="w-10 h-10 rounded-full border-2 border-white dark:border-slate-700 shadow-xs object-cover flex-shrink-0"
                            />
                            <div>
                              <p className={clsx(
                                "font-bold text-sm group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors flex items-center gap-2",
                                isMe ? "text-blue-700 dark:text-blue-400" : "text-gray-900 dark:text-white"
                              )}>
                                <span>{entry.userName}</span>
                                {isMe && (
                                  <span className="text-[10px] font-black bg-blue-100 text-blue-700 dark:bg-blue-900/60 dark:text-blue-300 px-2 py-0.5 rounded-full">
                                    You
                                  </span>
                                )}
                              </p>
                              <div className="flex items-center gap-1.5 mt-0.5">
                                <span className="inline-flex items-center gap-1 text-[10px] font-bold text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-950/40 px-2 py-0.5 rounded-md">
                                  <Zap className="w-2.5 h-2.5 fill-current" />
                                  Lv. {entry.level || 1} • {entry.title || 'Novice'}
                                </span>
                              </div>
                            </div>
                          </div>
                        </td>
                        <td className="py-4 px-6 text-center">
                          <span className="font-bold text-gray-700 dark:text-gray-300 text-sm">
                            {entry.tasksCompleted}
                          </span>
                        </td>
                        <td className="py-4 px-6 text-center">
                          <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-orange-50 dark:bg-orange-950/30 text-orange-600 dark:text-orange-400 rounded-full font-bold text-xs border border-orange-200/50 dark:border-orange-800/40">
                            <Flame className="w-3.5 h-3.5 fill-current" />
                            <span>{entry.currentStreak} Days</span>
                          </div>
                        </td>
                        <td className="py-4 px-6 text-right">
                          <div className="inline-flex items-center gap-1.5 font-black text-base text-amber-500">
                            <Star className="w-4 h-4 fill-current" />
                            <span>
                              <AnimatedNumber value={entry.totalXp} />
                            </span>
                          </div>
                        </td>
                      </motion.tr>
                    );
                  })}
                </tbody>
              </table>
              
              {filteredLeaders.length === 0 && (
                <div className="py-12 text-center text-gray-500 dark:text-gray-400 text-sm">
                  No members found matching your search.
                </div>
              )}
            </div>
          )}
        </Card>
      )}
    </div>
  );
};

export default LeaderboardPage;
