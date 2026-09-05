import React, { useEffect, useState, useMemo } from 'react';
import { api } from '../lib/api';
import { DashboardData, Task } from '../types';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ProgressRing from '../components/dashboard/ProgressRing';
import MiniLeaderboard from '../components/dashboard/MiniLeaderboard';
import AdminDailyPrepHub from '../components/dashboard/AdminDailyPrepHub';
import CoinRewardOverlay from '../components/common/CoinRewardOverlay';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import SolutionModal from '../components/tasks/SolutionModal';
import { ExternalLink, Star, Search, CheckCircle2, Circle, Trophy, Target } from 'lucide-react';
import { useToast } from '../components/ui/Toast';
import { toSafeUrl } from '../lib/security';

const DashboardPage: React.FC = () => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [difficultyFilter, setDifficultyFilter] = useState<'ALL' | 'EASY' | 'MEDIUM' | 'HARD'>('ALL');
  const [selectedTaskForCompletion, setSelectedTaskForCompletion] = useState<Task | null>(null);
  const [rewardXp, setRewardXp] = useState<number | null>(null);
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
    if (task.completed) {
      // Uncomplete directly
      try {
        await api.uncompleteTask(task.id);
        show('Problem marked incomplete', 'info');
        fetchDashboard();
      } catch (error) {
        show('Action failed', 'error');
      }
    } else {
      // Open solution modal for completing
      setSelectedTaskForCompletion(task);
    }
  };

  const handleModalSubmit = async (solutionLink: string, notes: string) => {
    if (!selectedTaskForCompletion) return;
    try {
      await api.completeTask(selectedTaskForCompletion.id, { solutionLink, notes });
      setRewardXp(selectedTaskForCompletion.xpReward);
      show(`Task completed! +${selectedTaskForCompletion.xpReward} XP earned ⭐`, 'success');
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

  if (loading) return <div className="py-20"><LoadingSpinner size="lg" /></div>;
  if (!data) return <div>Error loading dashboard.</div>;

  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <div className="space-y-8">
      {/* Welcome Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold dark:text-white tracking-tight flex items-center gap-2">
            <span>Welcome back, {data.userName.split(' ')[0]}!</span>
            <span className="text-2xl">👋</span>
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1 text-sm">
            Track your DSA challenge progress, maintain consistency, and elevate your coding skills.
          </p>
        </div>

        {/* Total XP Highlight Card */}
        <div className="flex items-center gap-3 bg-gradient-to-r from-amber-500/10 via-yellow-500/10 to-amber-500/5 px-4 py-2 rounded-2xl border border-amber-500/20 shadow-xs">
          <div className="p-2 rounded-xl bg-amber-500 text-slate-950 shadow-sm">
            <Star className="w-5 h-5 fill-current" />
          </div>
          <div>
            <div className="text-xs text-gray-500 dark:text-gray-400 font-semibold uppercase tracking-wider">Total XP</div>
            <div className="text-xl font-black text-gray-900 dark:text-white">{data.totalXp} XP</div>
          </div>
        </div>
      </div>

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
                filteredTasks.map((task) => (
                  <div
                    key={task.id}
                    className={`p-4 sm:p-5 transition-all flex items-center justify-between gap-4 ${
                      task.completed
                        ? 'bg-emerald-500/5 dark:bg-emerald-950/10'
                        : 'hover:bg-gray-50/80 dark:hover:bg-slate-800/40'
                    }`}
                  >
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1.5">
                        <a
                          href={toSafeUrl(task.platformLink)}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="font-semibold text-gray-900 dark:text-white hover:text-blue-600 dark:hover:text-blue-400 flex items-center gap-1.5 truncate text-base"
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
                        <span className="text-xs font-semibold text-amber-500 flex items-center gap-1">
                          <Star className="w-3.5 h-3.5 fill-current" /> {task.xpReward} XP
                        </span>
                      </div>
                    </div>

                    {/* Prominent Task Action Button (Completed or Mark Solved) */}
                    <button
                      onClick={() => handleTaskClick(task)}
                      className={`px-4 py-2 rounded-xl text-xs font-semibold flex items-center gap-2 flex-shrink-0 transition-all shadow-sm active:scale-95 ${
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
                    </button>
                  </div>
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
        <Card className="flex flex-col items-center justify-center p-6 bg-gradient-to-br from-white to-gray-50 dark:from-slate-800 dark:to-slate-800/80 border-0 shadow-lg shadow-gray-200/50 dark:shadow-none relative overflow-hidden">
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
            {data.tasksCompleted} of {data.totalTasks} challenges completed ({data.completionPercentage}%)
          </p>
        </Card>

        {/* Total XP & Ranking Card */}
        <Card className="flex flex-col justify-center p-6 bg-gradient-to-br from-indigo-500 to-purple-600 border-0 text-white relative overflow-hidden shadow-lg shadow-indigo-500/30">
          <div className="absolute top-0 right-0 p-4 opacity-20">
            <Star className="w-24 h-24" />
          </div>
          <div className="relative z-10">
            <div className="flex items-center gap-2 text-indigo-100 text-sm font-semibold mb-1">
              <Trophy className="w-4 h-4 text-amber-300" />
              <span>Leaderboard Standing</span>
            </div>
            <div className="flex items-center gap-3">
              <span className="text-5xl font-black">{data.totalXp}</span>
              <Star className="w-8 h-8 text-yellow-300 fill-yellow-300" />
            </div>
            <p className="mt-4 text-indigo-100 bg-white/20 inline-block px-3 py-1 rounded-full text-xs font-medium backdrop-blur-sm">
              Keep solving daily to climb the leaderboard!
            </p>
          </div>
        </Card>

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

      {/* Animated Golden Coin Credit Effect */}
      {rewardXp !== null && (
        <CoinRewardOverlay
          xp={rewardXp}
          onComplete={() => setRewardXp(null)}
        />
      )}
    </div>
  );
};

export default DashboardPage;
