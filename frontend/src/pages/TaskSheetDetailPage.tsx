import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../lib/api';
import { TaskSheet } from '../types';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { useToast } from '../components/ui/Toast';
import { ArrowLeft, Calendar, ExternalLink, Star, Check } from 'lucide-react';

const TaskSheetDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [sheet, setSheet] = useState<TaskSheet | null>(null);
  const [loading, setLoading] = useState(true);
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

  const toggleTaskCompletion = async (taskId: number, completed: boolean) => {
    if (!sheet) return;
    
    // Optimistic update
    const newTasks = sheet.tasks.map(t => 
      t.id === taskId ? { ...t, completed: !completed } : t
    );
    setSheet({ ...sheet, tasks: newTasks });

    try {
      if (completed) {
        await api.uncompleteTask(taskId);
      } else {
        await api.completeTask(taskId);
        show('Task completed! +XP', 'success');
      }
    } catch (error) {
      // Revert on error
      show('Action failed', 'error');
      fetchSheet();
    }
  };

  if (loading) return <div className="py-20"><LoadingSpinner size="lg" /></div>;
  if (!sheet) return null;

  const totalTasks = sheet.tasks.length;
  const completedTasks = sheet.tasks.filter(t => t.completed).length;
  const progress = totalTasks > 0 ? (completedTasks / totalTasks) * 100 : 0;
  
  const startDate = new Date(sheet.startDate).toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });
  const endDate = new Date(sheet.endDate).toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <button 
        onClick={() => navigate('/tasks')}
        className="flex items-center gap-2 text-sm font-medium text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white transition-colors"
      >
        <ArrowLeft className="w-4 h-4" /> Back to Task Sheets
      </button>

      <div className="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-gray-200 dark:border-slate-700 overflow-hidden">
        {/* Header */}
        <div className="p-6 sm:p-8 bg-gradient-to-br from-blue-50 to-white dark:from-slate-800 dark:to-slate-800/80 border-b border-gray-200 dark:border-slate-700 relative overflow-hidden">
          <div className="absolute top-0 right-0 w-64 h-64 bg-blue-500/5 rounded-full blur-3xl -mr-20 -mt-20"></div>
          
          <div className="relative z-10">
            <div className="flex flex-wrap items-center gap-3 mb-4">
              <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400">
                {sheet.sheetType}
              </Badge>
              <span className="flex items-center gap-1.5 text-sm font-medium text-gray-500 dark:text-gray-400">
                <Calendar className="w-4 h-4" /> {startDate} — {endDate}
              </span>
            </div>
            
            <h1 className="text-3xl font-bold dark:text-white mb-2">{sheet.title}</h1>
            <p className="text-gray-600 dark:text-gray-300 max-w-2xl">{sheet.description}</p>
            
            {/* Progress Bar */}
            <div className="mt-8 pt-6 border-t border-gray-200 dark:border-slate-700/50">
              <div className="flex justify-between items-end mb-2">
                <div>
                  <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Your Progress</p>
                  <p className="text-2xl font-bold dark:text-white">{Math.round(progress)}%</p>
                </div>
                <div className="text-sm font-medium text-gray-500 dark:text-gray-400">
                  {completedTasks} of {totalTasks} tasks
                </div>
              </div>
              <div className="w-full bg-gray-200 dark:bg-slate-700 rounded-full h-3 overflow-hidden">
                <div 
                  className="bg-emerald-500 h-full rounded-full transition-all duration-1000 ease-out relative" 
                  style={{ width: `${progress}%` }}
                >
                  <div className="absolute inset-0 bg-white/20 animate-pulse"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Tasks List */}
        <div className="p-0">
          <div className="divide-y divide-gray-100 dark:divide-slate-700/50">
            {sheet.tasks.map((task, index) => (
              <div key={task.id} className="p-4 sm:p-6 hover:bg-gray-50 dark:hover:bg-slate-800/50 transition-colors flex items-start gap-4 sm:gap-6 group">
                <button 
                  onClick={() => toggleTaskCompletion(task.id, task.completed)}
                  className={`mt-1 w-8 h-8 rounded-lg flex items-center justify-center border-2 flex-shrink-0 transition-all duration-200 ${
                    task.completed 
                      ? 'bg-emerald-500 border-emerald-500 text-white shadow-sm shadow-emerald-500/20' 
                      : 'border-gray-300 dark:border-gray-600 bg-white dark:bg-slate-800 hover:border-emerald-400 group-hover:scale-105'
                  }`}
                >
                  {task.completed && <Check className="w-5 h-5" strokeWidth={3} />}
                </button>
                
                <div className="flex-1 min-w-0">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-2">
                    <h3 className={`text-lg font-semibold transition-colors ${
                      task.completed ? 'text-gray-500 dark:text-gray-400 line-through' : 'text-gray-900 dark:text-white'
                    }`}>
                      {index + 1}. {task.title}
                    </h3>
                    
                    <a 
                      href={task.platformLink} 
                      target="_blank" 
                      rel="noopener noreferrer" 
                      className="inline-flex items-center gap-1.5 text-sm font-medium text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 bg-blue-50 dark:bg-blue-900/20 px-3 py-1 rounded-full w-max"
                    >
                      Solve Problem <ExternalLink className="w-3 h-3" />
                    </a>
                  </div>
                  
                  <p className={`text-sm mb-4 line-clamp-2 ${
                    task.completed ? 'text-gray-400 dark:text-gray-500' : 'text-gray-600 dark:text-gray-300'
                  }`}>
                    {task.description}
                  </p>
                  
                  <div className="flex flex-wrap items-center gap-2">
                    <Badge variant={task.difficulty.toLowerCase() as any}>{task.difficulty}</Badge>
                    <Badge color={task.topicColor}>{task.topicName}</Badge>
                    <span className="flex items-center gap-1 text-sm font-bold text-amber-500 bg-amber-50 dark:bg-amber-900/20 px-2 py-0.5 rounded-full ml-auto">
                      <Star className="w-3.5 h-3.5 fill-current" /> {task.xpReward} XP
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default TaskSheetDetailPage;
