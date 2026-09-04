import React, { useEffect, useState } from 'react';
import { api } from '../lib/api';
import { DashboardData } from '../types';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import StreakCard from '../components/dashboard/StreakCard';
import ProgressRing from '../components/dashboard/ProgressRing';
import MiniLeaderboard from '../components/dashboard/MiniLeaderboard';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import { ExternalLink, Star } from 'lucide-react';
import { useToast } from '../components/ui/Toast';

const DashboardPage: React.FC = () => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
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

  const toggleTaskCompletion = async (taskId: number, completed: boolean) => {
    if (!data || !data.activeSheet) return;
    
    // Optimistic update
    const newTasks = data.activeSheet.tasks.map(t => 
      t.id === taskId ? { ...t, completed: !completed } : t
    );
    
    setData({
      ...data,
      activeSheet: {
        ...data.activeSheet,
        tasks: newTasks
      }
    });

    try {
      if (completed) {
        await api.uncompleteTask(taskId);
      } else {
        await api.completeTask(taskId);
        show('Task completed! +XP', 'success');
      }
      // Refresh to get actual stats
      fetchDashboard();
    } catch (error) {
      // Revert on error
      show('Action failed', 'error');
      fetchDashboard();
    }
  };

  if (loading) return <div className="py-20"><LoadingSpinner size="lg" /></div>;
  if (!data) return <div>Error loading dashboard.</div>;

  return (
    <div className="space-y-6">
      <div className="mb-8">
        <h1 className="text-3xl font-bold dark:text-white">Welcome back, {data.userName.split(' ')[0]}! 👋</h1>
        <p className="text-gray-500 dark:text-gray-400 mt-1">Here's your DSA progress at a glance.</p>
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
              Keep solving to earn more!
            </p>
          </div>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content Area */}
        <div className="lg:col-span-2 space-y-6">
          
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold dark:text-white">Active Challenge</h2>
          </div>

          {data.activeSheet ? (
            <Card className="p-0 overflow-hidden border-t-4 border-t-blue-500">
              <div className="p-6 border-b border-gray-100 dark:border-slate-700 bg-gray-50/50 dark:bg-slate-800/50">
                <div className="flex justify-between items-start">
                  <div>
                    <Badge className="mb-2 bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400">{data.activeSheet.sheetType}</Badge>
                    <h3 className="text-lg font-bold dark:text-white">{data.activeSheet.title}</h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">{data.activeSheet.description}</p>
                  </div>
                </div>
              </div>
              <div className="divide-y divide-gray-100 dark:divide-slate-700/50">
                {data.activeSheet.tasks.map(task => (
                  <div key={task.id} className="p-4 hover:bg-gray-50 dark:hover:bg-slate-700/30 transition-colors flex items-center gap-4">
                    <button 
                      onClick={() => toggleTaskCompletion(task.id, task.completed)}
                      className={`w-6 h-6 rounded-md flex items-center justify-center border-2 flex-shrink-0 transition-colors ${
                        task.completed 
                          ? 'bg-emerald-500 border-emerald-500 text-white' 
                          : 'border-gray-300 dark:border-gray-600 hover:border-emerald-400'
                      }`}
                    >
                      {task.completed && <svg className="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" /></svg>}
                    </button>
                    
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <a href={task.platformLink} target="_blank" rel="noopener noreferrer" className="font-medium text-gray-900 dark:text-white hover:text-blue-600 dark:hover:text-blue-400 flex items-center gap-1 truncate">
                          {task.title}
                          <ExternalLink className="w-3 h-3 text-gray-400" />
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
                ))}
              </div>
            </Card>
          ) : (
            <Card className="p-8 text-center bg-gray-50 dark:bg-slate-800/50">
              <p className="text-gray-500 dark:text-gray-400">No active task sheet right now.</p>
            </Card>
          )}

        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <MiniLeaderboard />
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
