import React, { useEffect, useState, useMemo } from 'react';
import { api } from '../lib/api';
import { DashboardData, Task } from '../types';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import StreakCard from '../components/dashboard/StreakCard';
import ProgressRing from '../components/dashboard/ProgressRing';
import MiniLeaderboard from '../components/dashboard/MiniLeaderboard';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import ShareToWhatsApp from '../components/common/ShareToWhatsApp';
import SolutionModal from '../components/tasks/SolutionModal';
import { ExternalLink, Star, Search, CheckCircle2, Circle } from 'lucide-react';
import { useToast } from '../components/ui/Toast';
import { toSafeUrl } from '../lib/security';

const DashboardPage: React.FC = () => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [difficultyFilter, setDifficultyFilter] = useState<'ALL' | 'EASY' | 'MEDIUM' | 'HARD'>('ALL');
  const [selectedTaskForCompletion, setSelectedTaskForCompletion] = useState<Task | null>(null);
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

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold dark:text-white">Welcome back, {data.userName.split(' ')[0]}! 👋</h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">Here's your DSA progress and WhatsApp community updates.</p>
        </div>
        <ShareToWhatsApp
          title="Share Daily Streak"
          message={`🔥 DSA Buddies Check-in!\n👤 ${data.userName}\n⚡ Streak: ${data.currentStreak} Days\n⭐ Total XP: ${data.totalXp}\n✅ Solved: ${data.tasksCompleted}/${data.totalTasks} Problems\n\nKeep solving team! 💪`}
        />
      </div>

      {/* Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StreakCard currentStreak={data.currentStreak} maxStreak={data.maxStreak} />
        
        <Card className="flex flex-col items-center justify-center p-6 bg-gradient-to-br from-white to-gray-50 dark:from-slate-800 dark:to-slate-800/80 border-0 shadow-lg shadow-gray-200/50 dark:shadow-none relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-emerald-500/10 rounded-full blur-3xl -mr-10 -mt-10" />
          <h3 className="text-lg font-medium text-gray-700 dark:text-gray-200 mb-4">Problems Solved</h3>
          <ProgressRing 
            completed={data.tasksCompleted} 
            total={data.totalTasks} 
            size={120} 
            strokeWidth={12} 
          />
        </Card>

        <Card className="flex flex-col justify-center p-6 bg-gradient-to-br from-indigo-500 to-purple-600 border-0 text-white relative overflow-hidden shadow-lg shadow-indigo-500/30">
          <div className="absolute top-0 right-0 p-4 opacity-20">
            <Star className="w-24 h-24" />
          </div>
          <div className="relative z-10">
            <h3 className="text-lg font-medium text-indigo-100 mb-1">Total XP</h3>
            <div className="flex items-center gap-3">
              <span className="text-5xl font-bold">{data.totalXp}</span>
              <Star className="w-8 h-8 text-yellow-300 fill-yellow-300" />
            </div>
            <p className="mt-4 text-indigo-100 bg-white/20 inline-block px-3 py-1 rounded-full text-sm backdrop-blur-sm">
              Keep solving to climb the leaderboard!
            </p>
          </div>
        </Card>
      </div>

      {/* WhatsApp Community Banner */}
      <ShareToWhatsApp
        variant="banner"
        message={`🔥 WhatsApp DSA Group Update!\nI just hit a ${data.currentStreak}-day solving streak with ${data.totalXp} total XP!\nCheck the leaderboard and join the daily challenges.`}
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content Area */}
        <div className="lg:col-span-2 space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <h2 className="text-xl font-bold dark:text-white">Active Challenge Sheet</h2>
            
            {/* Search & Filter pills */}
            <div className="flex items-center gap-2 flex-wrap">
              <div className="relative">
                <Search className="w-3.5 h-3.5 absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  placeholder="Filter tasks..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-8 pr-3 py-1 text-xs rounded-lg border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-gray-900 dark:text-white outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>

              <div className="flex items-center gap-1 bg-gray-100 dark:bg-slate-800 p-0.5 rounded-lg text-[11px] font-medium">
                {(['ALL', 'EASY', 'MEDIUM', 'HARD'] as const).map((diff) => (
                  <button
                    key={diff}
                    onClick={() => setDifficultyFilter(diff)}
                    className={`px-2 py-0.5 rounded-md transition-all ${
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
            <Card className="p-0 overflow-hidden border-t-4 border-t-blue-500">
              <div className="p-6 border-b border-gray-100 dark:border-slate-700 bg-gray-50/50 dark:bg-slate-800/50 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <Badge className="mb-2 bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400">
                    {data.activeSheet.sheetType}
                  </Badge>
                  <h3 className="text-lg font-bold dark:text-white">{data.activeSheet.title}</h3>
                  <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">{data.activeSheet.description}</p>
                </div>
                <ShareToWhatsApp
                  variant="badge"
                  title="Share Sheet"
                  message={`📋 New DSA Sheet: "${data.activeSheet.title}"\n${data.activeSheet.description}\nLet's solve it together!`}
                />
              </div>

              <div className="divide-y divide-gray-100 dark:divide-slate-700/50">
                {filteredTasks.length > 0 ? (
                  filteredTasks.map((task) => (
                    <div
                      key={task.id}
                      className={`p-4 transition-colors flex items-center gap-4 ${
                        task.completed
                          ? 'bg-emerald-500/5 dark:bg-emerald-950/10'
                          : 'hover:bg-gray-50 dark:hover:bg-slate-700/30'
                      }`}
                    >
                      <button 
                        onClick={() => handleTaskClick(task)}
                        title={task.completed ? 'Click to unmark' : 'Click to complete problem'}
                        className={`w-6 h-6 rounded-md flex items-center justify-center border-2 flex-shrink-0 transition-all ${
                          task.completed 
                            ? 'bg-emerald-500 border-emerald-500 text-white shadow-sm shadow-emerald-500/30' 
                            : 'border-gray-300 dark:border-gray-600 hover:border-emerald-400 hover:bg-emerald-50 dark:hover:bg-slate-800'
                        }`}
                      >
                        {task.completed ? (
                          <CheckCircle2 className="w-4 h-4 fill-white text-emerald-500" />
                        ) : (
                          <Circle className="w-4 h-4 text-transparent" />
                        )}
                      </button>
                      
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          <a
                            href={toSafeUrl(task.platformLink)}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="font-medium text-gray-900 dark:text-white hover:text-blue-600 dark:hover:text-blue-400 flex items-center gap-1.5 truncate"
                          >
                            <span className={task.completed ? 'line-through text-gray-500 dark:text-gray-400' : ''}>
                              {task.title}
                            </span>
                            <ExternalLink className="w-3.5 h-3.5 text-gray-400 flex-shrink-0" />
                          </a>
                        </div>
                        <div className="flex items-center gap-2 flex-wrap">
                          <Badge variant={task.difficulty.toLowerCase() as any}>{task.difficulty}</Badge>
                          <Badge color={task.topicColor}>{task.topicName}</Badge>
                          <span className="text-xs font-medium text-amber-500 flex items-center gap-1">
                            <Star className="w-3 h-3 fill-current" /> {task.xpReward} XP
                          </span>
                        </div>
                      </div>
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
              <p className="text-gray-500 dark:text-gray-400">No active task sheet right now.</p>
            </Card>
          )}
        </div>

        {/* Sidebar Mini-Leaderboard */}
        <div className="space-y-6">
          <MiniLeaderboard />
        </div>
      </div>

      {/* Solution & Notes Modal */}
      <SolutionModal
        task={selectedTaskForCompletion}
        isOpen={selectedTaskForCompletion !== null}
        onClose={() => setSelectedTaskForCompletion(null)}
        onSubmit={handleModalSubmit}
      />
    </div>
  );
};

export default DashboardPage;
