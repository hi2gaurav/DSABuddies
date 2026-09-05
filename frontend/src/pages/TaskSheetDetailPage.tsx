import React, { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../lib/api';
import { TaskSheet, Task } from '../types';
import Badge from '../components/ui/Badge';
import Card from '../components/ui/Card';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import CoinRewardOverlay from '../components/common/CoinRewardOverlay';
import SolutionModal from '../components/tasks/SolutionModal';
import { useToast } from '../components/ui/Toast';
import { ArrowLeft, Calendar, ExternalLink, Star, CheckCircle2, Circle, Search } from 'lucide-react';
import { toSafeUrl } from '../lib/security';

const TaskSheetDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [sheet, setSheet] = useState<TaskSheet | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [difficultyFilter, setDifficultyFilter] = useState<'ALL' | 'EASY' | 'MEDIUM' | 'HARD'>('ALL');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'SOLVED' | 'UNSOLVED'>('ALL');
  const [selectedTaskForCompletion, setSelectedTaskForCompletion] = useState<Task | null>(null);
  const [rewardXp, setRewardXp] = useState<number | null>(null);
  const { show } = useToast();

  const fetchSheet = async () => {
    try {
      const data = await api.getTaskSheet(Number(id));
      setSheet(data);
    } catch (error) {
      show('Failed to load task sheet', 'error');
      navigate('/tasks');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSheet();
  }, [id]);

  const handleTaskClick = async (task: Task) => {
    if (task.completed) {
      try {
        await api.uncompleteTask(task.id);
        show('Problem marked incomplete', 'info');
        fetchSheet();
      } catch (error) {
        show('Action failed', 'error');
      }
    } else {
      setSelectedTaskForCompletion(task);
    }
  };

  const handleModalSubmit = async (solutionLink: string, notes: string) => {
    if (!selectedTaskForCompletion) return;
    try {
      await api.completeTask(selectedTaskForCompletion.id, { solutionLink, notes });
      setRewardXp(selectedTaskForCompletion.xpReward);
      show(`Task completed! +${selectedTaskForCompletion.xpReward} XP earned ⭐`, 'success');
      fetchSheet();
    } catch (error) {
      show('Failed to complete task', 'error');
    }
  };

  const filteredTasks = useMemo(() => {
    if (!sheet?.tasks) return [];
    return sheet.tasks.filter((t) => {
      const matchesSearch = t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                            t.topicName.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesDiff = difficultyFilter === 'ALL' || t.difficulty === difficultyFilter;
      const matchesStatus = statusFilter === 'ALL' ||
                            (statusFilter === 'SOLVED' && t.completed) ||
                            (statusFilter === 'UNSOLVED' && !t.completed);
      return matchesSearch && matchesDiff && matchesStatus;
    });
  }, [sheet?.tasks, searchQuery, difficultyFilter, statusFilter]);

  if (loading) return <div className="py-20"><LoadingSpinner size="lg" /></div>;
  if (!sheet) return null;

  const totalTasks = sheet.tasks.length;
  const completedTasks = sheet.tasks.filter((t) => t.completed).length;
  const progressPercent = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
  const totalXp = sheet.tasks.reduce((sum, t) => sum + t.xpReward, 0);
  const earnedXp = sheet.tasks.filter((t) => t.completed).reduce((sum, t) => sum + t.xpReward, 0);

  return (
    <div className="space-y-6">
      {/* Back button & header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <button
          onClick={() => navigate('/tasks')}
          className="inline-flex items-center gap-1.5 text-sm font-medium text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to All Sheets</span>
        </button>
      </div>

      {/* Sheet Overview Card */}
      <Card className="p-6 bg-gradient-to-br from-white to-gray-50 dark:from-slate-800 dark:to-slate-850 border-slate-200 dark:border-slate-700 shadow-md">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2 max-w-2xl">
            <div className="flex items-center gap-2">
              <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300 font-semibold">
                {sheet.sheetType}
              </Badge>
              <span className="text-xs text-gray-500 dark:text-gray-400 flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5" />
                {sheet.startDate} to {sheet.endDate}
              </span>
            </div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{sheet.title}</h1>
            <p className="text-sm text-gray-600 dark:text-gray-300">{sheet.description}</p>
          </div>

          {/* Mini Stats Banner */}
          <div className="flex items-center gap-4 border-t md:border-t-0 md:border-l border-gray-200 dark:border-slate-700 pt-4 md:pt-0 md:pl-6 flex-shrink-0">
            <div className="text-center">
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {completedTasks}/{totalTasks}
              </div>
              <div className="text-xs text-gray-500 dark:text-gray-400">Problems</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-amber-500">
                {earnedXp}/{totalXp}
              </div>
              <div className="text-xs text-gray-500 dark:text-gray-400">XP Earned</div>
            </div>
          </div>
        </div>

        {/* Progress Bar */}
        <div className="mt-6 pt-4 border-t border-gray-100 dark:border-slate-700/60">
          <div className="flex items-center justify-between text-xs font-semibold mb-1.5">
            <span className="text-gray-700 dark:text-gray-300">Completion Status</span>
            <span className="text-emerald-600 dark:text-emerald-400">{progressPercent}%</span>
          </div>
          <div className="w-full bg-gray-200 dark:bg-slate-700 h-2.5 rounded-full overflow-hidden">
            <div
              className="bg-emerald-500 h-full rounded-full transition-all duration-500"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>
      </Card>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search problems by name or topic..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-sm rounded-xl border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          {/* Difficulty filter */}
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

          {/* Completion status filter */}
          <div className="flex items-center gap-1 bg-gray-100 dark:bg-slate-800 p-1 rounded-xl text-xs font-medium">
            {(['ALL', 'SOLVED', 'UNSOLVED'] as const).map((status) => (
              <button
                key={status}
                onClick={() => setStatusFilter(status)}
                className={`px-2.5 py-1 rounded-lg transition-all ${
                  statusFilter === status
                    ? 'bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-xs font-semibold'
                    : 'text-gray-500 dark:text-gray-400 hover:text-gray-700'
                }`}
              >
                {status === 'ALL' ? 'All Status' : status === 'SOLVED' ? 'Solved' : 'Pending'}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Task List */}
      <Card className="p-0 overflow-hidden divide-y divide-gray-100 dark:divide-slate-700/60 shadow-md">
        {filteredTasks.length > 0 ? (
          filteredTasks.map((task) => (
            <div
              key={task.id}
              className={`p-4 sm:p-5 transition-colors flex items-center justify-between gap-4 ${
                task.completed
                  ? 'bg-emerald-500/5 dark:bg-emerald-950/10'
                  : 'hover:bg-gray-50/80 dark:hover:bg-slate-800/40'
              }`}
            >
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
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

              {/* Status Action Button */}
              <button
                onClick={() => handleTaskClick(task)}
                className={`px-3.5 py-2 rounded-xl text-xs font-semibold flex items-center gap-1.5 flex-shrink-0 transition-all shadow-sm ${
                  task.completed
                    ? 'bg-emerald-500 hover:bg-emerald-600 text-white shadow-emerald-500/20 active:scale-95'
                    : 'bg-white dark:bg-slate-800 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 border border-gray-200 dark:border-slate-700 hover:border-emerald-400 text-gray-700 dark:text-gray-200 hover:text-emerald-600 dark:hover:text-emerald-400 active:scale-95'
                }`}
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
          <div className="p-12 text-center">
            <p className="text-gray-500 dark:text-gray-400 text-sm">No challenges match your search filters.</p>
          </div>
        )}
      </Card>

      {/* Solution & Notes Modal */}
      <SolutionModal
        task={selectedTaskForCompletion}
        isOpen={selectedTaskForCompletion !== null}
        onClose={() => setSelectedTaskForCompletion(null)}
        onSubmit={handleModalSubmit}
      />

      {/* Coin Reward Animation */}
      {rewardXp !== null && (
        <CoinRewardOverlay
          xp={rewardXp}
          onComplete={() => setRewardXp(null)}
        />
      )}
    </div>
  );
};

export default TaskSheetDetailPage;
