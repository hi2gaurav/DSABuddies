import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../lib/api';
import { TaskSheet } from '../types';
import { useAuth } from '../hooks/useAuth';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { Calendar, CheckCircle2, ChevronRight } from 'lucide-react';

const TasksPage: React.FC = () => {
  const [sheets, setSheets] = useState<TaskSheet[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'active'>('all');
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    const fetchSheets = async () => {
      try {
        const data = await api.getTaskSheets(filter === 'active');
        setSheets(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchSheets();
  }, [filter]);

  if (loading) return <div className="py-20"><LoadingSpinner size="lg" /></div>;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold dark:text-white">Task Sheets</h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">Complete tasks to earn XP and build your streak.</p>
        </div>
        
        <div className="flex items-center gap-3">
          <select 
            value={filter}
            onChange={(e) => setFilter(e.target.value as 'all' | 'active')}
            className="bg-white dark:bg-slate-800 border border-gray-300 dark:border-slate-700 text-gray-900 dark:text-white rounded-lg px-4 py-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="all">All Sheets</option>
            <option value="active">Active Only</option>
          </select>
          
          {user?.role === 'ROLE_ADMIN' && (
            <button 
              onClick={() => navigate('/admin')}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium transition-colors"
            >
              + New Sheet
            </button>
          )}
        </div>
      </div>

      {sheets.length === 0 ? (
        <Card className="p-12 text-center bg-gray-50 dark:bg-slate-800/50">
          <div className="bg-gray-200 dark:bg-slate-700 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
            <CheckCircle2 className="w-8 h-8 text-gray-400 dark:text-slate-500" />
          </div>
          <h3 className="text-xl font-bold dark:text-white mb-2">No task sheets found</h3>
          <p className="text-gray-500 dark:text-gray-400">
            {filter === 'active' ? "There are no active challenges right now." : "Check back later for new tasks."}
          </p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          {sheets.map((sheet) => {
            const totalTasks = sheet.tasks.length;
            const completedTasks = sheet.tasks.filter(t => t.completed).length;
            const progress = totalTasks > 0 ? (completedTasks / totalTasks) * 100 : 0;
            
            const startDate = new Date(sheet.startDate).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
            const endDate = new Date(sheet.endDate).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
            
            const isActive = new Date(sheet.startDate) <= new Date() && new Date(sheet.endDate) >= new Date();

            return (
              <Card 
                key={sheet.id} 
                hover 
                onClick={() => navigate(`/tasks/${sheet.id}`)}
                className="flex flex-col h-full"
              >
                <div className="p-6 flex-1">
                  <div className="flex justify-between items-start mb-4">
                    <Badge variant="default" className="bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400">
                      {sheet.sheetType}
                    </Badge>
                    {isActive && (
                      <span className="flex items-center gap-1 text-xs font-medium text-emerald-600 dark:text-emerald-400">
                        <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                        Active
                      </span>
                    )}
                  </div>
                  
                  <h3 className="text-xl font-bold dark:text-white mb-2">{sheet.title}</h3>
                  <p className="text-sm text-gray-500 dark:text-gray-400 line-clamp-2 mb-4">
                    {sheet.description}
                  </p>
                  
                  <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400 mb-6">
                    <Calendar className="w-4 h-4" />
                    {startDate} - {endDate}
                  </div>
                  
                  <div className="mt-auto">
                    <div className="flex justify-between text-sm mb-1">
                      <span className="font-medium text-gray-700 dark:text-gray-300">Progress</span>
                      <span className="text-gray-500 dark:text-gray-400">{completedTasks}/{totalTasks}</span>
                    </div>
                    <div className="w-full bg-gray-200 dark:bg-slate-700 rounded-full h-2">
                      <div 
                        className="bg-emerald-500 h-2 rounded-full transition-all duration-500" 
                        style={{ width: `${progress}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
                
                <div className="px-6 py-3 bg-gray-50 dark:bg-slate-800/80 border-t border-gray-100 dark:border-slate-700/50 flex justify-between items-center text-sm font-medium text-blue-600 dark:text-blue-400 group">
                  View Tasks
                  <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                </div>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default TasksPage;
