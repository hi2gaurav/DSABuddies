import React, { useEffect, useState, useMemo } from 'react';
import { motion } from 'framer-motion';
import { api } from '../lib/api';
import { DashboardData, Task } from '../types';
import { useAuth } from '../hooks/useAuth';
import { DashboardSkeleton } from '../components/common/SkeletonLoader';
import AnimatedNumber from '../components/common/AnimatedNumber';
import ProgressRing from '../components/dashboard/ProgressRing';
import MiniLeaderboard from '../components/dashboard/MiniLeaderboard';
import AdminDailyPrepHub from '../components/dashboard/AdminDailyPrepHub';
import CoinRewardOverlay from '../components/common/CoinRewardOverlay';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import SolutionModal from '../components/tasks/SolutionModal';
import BookmarkButton from '../components/common/BookmarkButton';
import NoteModal from '../components/common/NoteModal';
import BadgeCelebrationModal from '../components/common/BadgeCelebrationModal';
import LevelUpModal from '../components/common/LevelUpModal';
import { ExternalLink, Star, Search, CheckCircle2, Circle, Trophy, Target, Zap, FileText } from 'lucide-react';
import { useToast } from '../components/ui/Toast';
import { toSafeUrl } from '../lib/security';
import { Badge as BadgeType } from '../types';
import { AnnouncementBanner } from '../components/common/AnnouncementBanner';

const DashboardPage: React.FC = () => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [difficultyFilter, setDifficultyFilter] = useState<'ALL' | 'EASY' | 'MEDIUM' | 'HARD'>('ALL');
  const [selectedTaskForCompletion, setSelectedTaskForCompletion] = useState<Task | null>(null);
  const [selectedNoteTask, setSelectedNoteTask] = useState<{ id: number; title: string } | null>(null);
  const [rewardXp, setRewardXp] = useState<number | null>(null);
  const [newBadges, setNewBadges] = useState<BadgeType[]>([]);
  const [levelUpData, setLevelUpData] = useState<{ level: number; title: string } | null>(null);
  const { user } = useAuth();
  const { show } = useToast();

  const fetchDashboard = async () => {
    try {
      const dashboardData = await api.getDashboard();
      setData(dashboardData);
    } catch (error) {
      show('Failed to load dashboard data', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const handleTaskClick = async (task: Task) => {
    const wasCompleted = task.completed;

    // 1. Instant optimistic state update — zero lag, button turns green immediately
    setData((prev) => {
      if (!prev) return prev;
      const updatedTasks = prev.activeSheet?.tasks.map((t) =>
        t.id === task.id ? { ...t, completed: !wasCompleted } : t
      ) || [];
      const newCompletedCount = updatedTasks.filter((t) => t.completed).length;
      const total = prev.totalTasks || updatedTasks.length || 1;
      const newPct = Math.round((newCompletedCount / total) * 100);
      const xpDelta = wasCompleted ? -task.xpReward : task.xpReward;

      return {
        ...prev,
        tasksCompleted: newCompletedCount,
        completionPercentage: newPct,
        totalXp: Math.max(0, prev.totalXp + xpDelta),
        activeSheet: prev.activeSheet ? { ...prev.activeSheet, tasks: updatedTasks } : prev.activeSheet,
      };
    });

    if (wasCompleted) {
      // Uncomplete
      try {
        await api.uncompleteTask(task.id);
        show('Problem marked incomplete', 'info');
        fetchDashboard();
      } catch (error) {
        show('Failed to update status, reverting...', 'error');
        fetchDashboard();
      }
    } else {
      // Mark completed immediately — no delay, no full-screen blur modal
      setRewardXp(task.xpReward);
      show(`Task completed! +${task.xpReward} XP earned ⭐`, 'success');

      try {
        const res = await api.completeTask(task.id, {});
        if (res.levelUp) {
          setLevelUpData({ level: res.newLevel || 1, title: res.newTitle || 'Novice' });
        }
        if (res.newBadges && res.newBadges.length > 0) {
          setNewBadges(res.newBadges);
        }
        fetchDashboard();
      } catch (error) {
        show('Failed to complete task, reverting...', 'error');
        fetchDashboard();
      }
    }
  };

  const handleModalSubmit = async (solutionLink: string, notes: string) => {
    if (!selectedTaskForCompletion) return;
    try {
      const res = await api.completeTask(selectedTaskForCompletion.id, { solutionLink, notes });
      setRewardXp(selectedTaskForCompletion.xpReward);
      show(`Task completed! +${selectedTaskForCompletion.xpReward} XP earned ⭐`, 'success');
      if (res.levelUp) {
        setLevelUpData({ level: res.newLevel || 1, title: res.newTitle || 'Novice' });
      }
      if (res.newBadges && res.newBadges.length > 0) {
        setNewBadges(res.newBadges);
      }
      fetchDashboard();
    } catch (error) {
      show('Failed to complete task', 'error');
    }
  };

  const filteredTasks = useMemo(() => {
    if (!data?.activeSheet?.tasks) return [];
    return data.activeSheet.tasks.filter((t) => {
      const matchesSearch = t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                            t.topicName.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesDiff = difficultyFilter === 'ALL' || t.difficulty === difficultyFilter;
      return matchesSearch && matchesDiff;
    });
  }, [data?.activeSheet?.tasks, searchQuery, difficultyFilter]);

  if (loading) return <DashboardSkeleton />;
  if (!data) return <div className="p-8 text-center text-gray-500">Error loading dashboard.</div>;

  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <div className="space-y-8">
      {/* Broadcast Announcements */}
      <AnnouncementBanner />

      {/* Welcome Banner */}
      <motion.div 
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35, ease: 'easeOut' }}
        className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 p-6 rounded-3xl bg-gradient-to-r from-blue-600/10 via-indigo-600/5 to-purple-600/10 border border-blue-500/20 shadow-xs backdrop-blur-xs"
      >
        <div>
          <div className="flex items-center gap-2 mb-1 flex-wrap">
            <span className="px-2.5 py-0.5 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-600 dark:text-blue-400 text-xs font-black tracking-wider uppercase inline-flex items-center gap-1">
              <Zap className="w-3 h-3 fill-current" /> Daily Prep
            </span>
            <span className="px-2.5 py-0.5 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-600 dark:text-indigo-400 text-xs font-black tracking-wider inline-flex items-center gap-1">
              Lv. {user?.level || 1} • {user?.title || 'Novice'}
            </span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-gray-900 dark:text-white tracking-tight flex items-center gap-2">
            <span>Welcome back, {data.userName.split(' ')[0]}!</span>
            <span className="text-2xl inline-block">👋</span>
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1 text-xs sm:text-sm max-w-xl">
            Track your DSA challenge progress, maintain consistency, and elevate your coding skills.
          </p>
        </div>

        {/* Total XP Highlight Card */}
        <motion.div 
          whileHover={{ scale: 1.04 }}
          className="flex items-center gap-3.5 bg-gradient-to-r from-amber-500/15 via-yellow-500/10 to-amber-500/5 px-5 py-3 rounded-2xl border border-amber-500/30 shadow-xs"
        >
          <div className="p-2.5 rounded-xl bg-amber-500 text-slate-950 shadow-md shadow-amber-500/30">
            <Star className="w-5 h-5 fill-current" />
          </div>
          <div>
            <div className="text-[10px] text-gray-500 dark:text-gray-400 font-bold uppercase tracking-wider">Total XP</div>
            <div className="text-xl sm:text-2xl font-black text-gray-900 dark:text-white">
              <AnimatedNumber value={data.totalXp} suffix=" XP" />
            </div>
          </div>
        </motion.div>
      </motion.div>

      {/* ADMIN-ONLY ME SECTION: DAILY PREP HUB */}
      {isAdmin && (
        <section className="space-y-2">
          <AdminDailyPrepHub />
        </section>
      )}

      {/* TODAY'S TASKS / ACTIVE CHALLENGE SHEET (DISPLAYED FIRST) */}
      <section className="space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <Target className="w-6 h-6 text-blue-500" />
            <h2 className="text-2xl font-bold dark:text-white">Today's Tasks & Active Sheet</h2>
            {data.activeSheet && (
              <span className="text-xs px-2.5 py-0.5 rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300 font-bold">
                {data.tasksCompleted}/{data.totalTasks} Solved
              </span>
            )}
          </div>

          {/* Search & Filter pills */}
          <div className="flex items-center gap-2 flex-wrap">
            <div className="relative">
              <Search className="w-3.5 h-3.5 absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="Filter tasks..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-8 pr-3 py-1.5 text-xs rounded-xl border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-blue-500 shadow-xs"
              />
            </div>

            <div className="flex items-center gap-1 bg-gray-100 dark:bg-slate-800 p-1 rounded-xl text-xs font-medium">
              {(['ALL', 'EASY', 'MEDIUM', 'HARD'] as const).map((diff) => (
                <button
                  key={diff}
                  onClick={() => setDifficultyFilter(diff)}
                  className={`px-2.5 py-1 rounded-lg transition-all ${
                    difficultyFilter === diff
                      ? 'bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-xs font-semibold'
                      : 'text-gray-500 dark:text-gray-400 hover:text-gray-700'
                  }`}
                >
                  {diff === 'ALL' ? 'All' : diff.charAt(0) + diff.slice(1).toLowerCase()}
                </button>
              ))}
            </div>
          </div>
        </div>

        {data.activeSheet ? (
          <Card className="p-0 overflow-hidden border-t-4 border-t-blue-500 shadow-lg">
            <div className="p-5 border-b border-gray-100 dark:border-slate-700 bg-gray-50/50 dark:bg-slate-800/50 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <Badge className="mb-2 bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400 font-bold">
                  {data.activeSheet.sheetType}
                </Badge>
                <h3 className="text-xl font-bold dark:text-white">{data.activeSheet.title}</h3>
                <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{data.activeSheet.description}</p>
              </div>

              <div className="text-xs text-gray-500 dark:text-gray-400 flex items-center gap-1">
                <span>Created by:</span>
                <span className="font-semibold text-gray-700 dark:text-gray-200">{data.activeSheet.createdByName}</span>
              </div>
            </div>

            <div className="divide-y divide-gray-100 dark:divide-slate-700/50">
              {filteredTasks.length > 0 ? (
                filteredTasks.map((task, idx) => (
                  <motion.div
                    key={task.id}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.25, delay: idx * 0.04 }}
                    className={`p-4 sm:p-5 transition-all flex items-center justify-between gap-4 ${
                      task.completed
                        ? 'bg-emerald-500/5 dark:bg-emerald-950/10'
                        : 'hover:bg-blue-50/40 dark:hover:bg-slate-800/50'
                    }`}
                  >
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1.5">
                        <a
                          href={toSafeUrl(task.platformLink)}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="font-bold text-gray-900 dark:text-white hover:text-blue-600 dark:hover:text-blue-400 flex items-center gap-1.5 truncate text-base transition-colors"
                        >
                          <span className={task.completed ? 'line-through text-gray-400 dark:text-gray-500' : ''}>
                            {task.title}
                          </span>
                          <ExternalLink className="w-3.5 h-3.5 text-gray-400 flex-shrink-0" />
                        </a>
                      </div>

                      {task.description && (
                        <p className="text-xs text-gray-500 dark:text-gray-400 line-clamp-1 mb-2">
                          {task.description}
                        </p>
                      )}

                      <div className="flex items-center gap-2 flex-wrap">
                        <Badge variant={task.difficulty.toLowerCase() as any}>{task.difficulty}</Badge>
                        <Badge color={task.topicColor}>{task.topicName}</Badge>
                        <span className="text-xs font-bold text-amber-500 flex items-center gap-1">
                          <Star className="w-3.5 h-3.5 fill-current" /> {task.xpReward} XP
                        </span>
                      </div>
                    </div>

                    {/* Task Actions: Bookmark, Notes, Complete */}
                    <div className="flex items-center gap-1.5 flex-shrink-0">
                      <BookmarkButton taskId={task.id} />

                      <motion.button
                        whileHover={{ scale: 1.1 }}
                        whileTap={{ scale: 0.9 }}
                        type="button"
                        onClick={() => setSelectedNoteTask({ id: task.id, title: task.title })}
                        className="p-1.5 rounded-lg text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 hover:bg-gray-100 dark:hover:bg-slate-800 transition-colors"
                        title="Personal Notes & Code"
                      >
                        <FileText className="w-4 h-4" />
                      </motion.button>

                      {/* Prominent Task Action Button (Completed or Mark Solved) */}
                      <motion.button
                        whileHover={{ scale: 1.04 }}
                        whileTap={{ scale: 0.94 }}
                        onClick={() => handleTaskClick(task)}
                        className={`px-3.5 py-2 rounded-xl text-xs font-bold flex items-center gap-2 transition-all shadow-xs ${
                          task.completed
                            ? 'bg-emerald-500 hover:bg-emerald-600 text-white shadow-emerald-500/25 ring-2 ring-emerald-500/20'
                            : 'bg-white dark:bg-slate-800 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 border border-gray-200 dark:border-slate-700 hover:border-emerald-400 text-gray-700 dark:text-gray-200 hover:text-emerald-600 dark:hover:text-emerald-400'
                        }`}
                        title={task.completed ? 'Click to unmark problem' : 'Click to complete problem and submit notes'}
                      >
                        {task.completed ? (
                          <>
                            <CheckCircle2 className="w-4 h-4 fill-white text-emerald-500" />
                            <span>Completed ✓</span>
                          </>
                        ) : (
                          <>
                            <Circle className="w-4 h-4 text-gray-400" />
                            <span>Mark Solved</span>
                          </>
                        )}
                      </motion.button>
                    </div>
                  </motion.div>
                ))
              ) : (
                <div className="p-8 text-center text-gray-400 text-xs">
                  No problems match your current search/filter.
                </div>
              )}
            </div>
          </Card>
        ) : (
          <Card className="p-8 text-center bg-gray-50 dark:bg-slate-800/50">
            <p className="text-gray-500 dark:text-gray-400 text-sm">No active task sheet right now.</p>
          </Card>
        )}
      </section>

      {/* STATS & COMMUNITY SECTION */}
      <section className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Solved Problems Progress */}
        <motion.div whileHover={{ y: -4 }} transition={{ duration: 0.2 }}>
          <Card className="flex flex-col items-center justify-center p-6 bg-gradient-to-br from-white to-gray-50 dark:from-slate-800 dark:to-slate-800/80 border border-gray-100 dark:border-slate-700/60 shadow-lg shadow-gray-200/50 dark:shadow-none relative overflow-hidden h-full">
            <div className="absolute top-0 right-0 w-32 h-32 bg-emerald-500/10 rounded-full blur-3xl -mr-10 -mt-10" />
            <h3 className="text-base font-bold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
              <Target className="w-4 h-4 text-emerald-500" />
              <span>Problem Solving Progress</span>
            </h3>
            <ProgressRing 
              completed={data.tasksCompleted} 
              total={data.totalTasks} 
              size={120} 
              strokeWidth={12} 
            />
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-4 text-center">
              <span className="font-bold text-gray-800 dark:text-gray-200">
                <AnimatedNumber value={data.tasksCompleted} />
              </span> of {data.totalTasks} challenges completed ({data.completionPercentage}%)
            </p>
          </Card>
        </motion.div>

        {/* Total XP & Ranking Card */}
        <motion.div whileHover={{ y: -4 }} transition={{ duration: 0.2 }}>
          <Card className="flex flex-col justify-center p-6 bg-gradient-to-br from-indigo-600 via-purple-600 to-indigo-700 border-0 text-white relative overflow-hidden shadow-xl shadow-indigo-500/25 h-full">
            <div className="absolute top-0 right-0 p-4 opacity-15">
              <Star className="w-24 h-24" />
            </div>
            <div className="relative z-10">
              <div className="flex items-center gap-2 text-indigo-100 text-xs font-black uppercase tracking-wider mb-1">
                <Trophy className="w-4 h-4 text-amber-300" />
                <span>Leaderboard Standing</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-5xl font-black">
                  <AnimatedNumber value={data.totalXp} />
                </span>
                <Star className="w-8 h-8 text-yellow-300 fill-yellow-300 animate-pulse" />
              </div>
              <p className="mt-4 text-indigo-100 bg-white/20 inline-block px-3 py-1 rounded-full text-xs font-semibold backdrop-blur-xs">
                Keep solving daily to climb the leaderboard!
              </p>
            </div>
          </Card>
        </motion.div>

        {/* Sidebar Mini-Leaderboard */}
        <div>
          <MiniLeaderboard />
        </div>
      </section>


      {/* Solution & Notes Modal */}
      <SolutionModal
        task={selectedTaskForCompletion}
        isOpen={selectedTaskForCompletion !== null}
        onClose={() => setSelectedTaskForCompletion(null)}
        onSubmit={handleModalSubmit}
      />

      {/* Personal Notes & Code Modal */}
      {selectedNoteTask && (
        <NoteModal
          taskId={selectedNoteTask.id}
          taskTitle={selectedNoteTask.title}
          isOpen={true}
          onClose={() => setSelectedNoteTask(null)}
        />
      )}

      {/* Animated Golden Coin Credit Effect */}
      {rewardXp !== null && (
        <CoinRewardOverlay
          xp={rewardXp}
          onComplete={() => setRewardXp(null)}
        />
      )}

      {/* Level Up Celebration Modal */}
      {levelUpData && (
        <LevelUpModal
          level={levelUpData.level}
          title={levelUpData.title}
          isOpen={levelUpData !== null}
          onClose={() => setLevelUpData(null)}
        />
      )}

      {/* Badge Celebration Modal */}
      {newBadges.length > 0 && (
        <BadgeCelebrationModal
          badges={newBadges}
          isOpen={newBadges.length > 0}
          onClose={() => setNewBadges([])}
        />
      )}
    </div>
  );
};

export default DashboardPage;
