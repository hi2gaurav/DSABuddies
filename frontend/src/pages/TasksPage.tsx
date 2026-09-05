import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../lib/api';
import { TaskSheet } from '../types';
import { useAuth } from '../hooks/useAuth';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { motion } from 'framer-motion';
import { Calendar, CheckCircle2, ChevronRight, Search, Plus } from 'lucide-react';

const TasksPage: React.FC = () => {
  const [sheets, setSheets] = useState<TaskSheet[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState<'ALL' | 'ACTIVE' | 'DAILY' | 'WEEKLY'>('ALL');
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    const fetchSheets = async () => {
      try {
        const data = await api.getTaskSheets();
        setSheets(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchSheets();
  }, []);

  const filteredSheets = useMemo(() => {
    return sheets.filter((sheet) => {
      const matchesSearch = sheet.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                            (sheet.description && sheet.description.toLowerCase().includes(searchQuery.toLowerCase()));
      
      const now = new Date();
      const isActive = new Date(sheet.startDate) <= now && new Date(sheet.endDate) >= now;

      let matchesType = true;
      if (typeFilter === 'ACTIVE') matchesType = isActive;
      else if (typeFilter === 'DAILY') matchesType = sheet.sheetType === 'DAILY';
      else if (typeFilter === 'WEEKLY') matchesType = sheet.sheetType === 'WEEKLY';

      return matchesSearch && matchesType;
    });
  }, [sheets, searchQuery, typeFilter]);

  if (loading) return <div className="py-20"><LoadingSpinner size="lg" /></div>;

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold dark:text-white">Task Sheets</h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Browse daily and weekly challenges, track your group problem-solving.
          </p>
        </div>
        
        <div className="flex items-center gap-3 flex-wrap">
          {user?.role === 'ROLE_ADMIN' && (
            <button 
              onClick={() => navigate('/admin')}
              className="inline-flex items-center gap-1.5 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-xl text-sm font-semibold shadow-sm transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Create Sheet</span>
            </button>
          )}
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search sheets by title or topic..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-sm rounded-xl border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div className="flex items-center gap-1 bg-gray-100 dark:bg-slate-800 p-1 rounded-xl text-xs font-medium">
          {(['ALL', 'ACTIVE', 'DAILY', 'WEEKLY'] as const).map((type) => (
            <button
              key={type}
              onClick={() => setTypeFilter(type)}
              className={`px-3 py-1.5 rounded-lg transition-all ${
                typeFilter === type
                  ? 'bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-xs font-semibold'
                  : 'text-gray-500 dark:text-gray-400 hover:text-gray-700'
              }`}
            >
              {type === 'ALL' ? 'All Sheets' : type === 'ACTIVE' ? 'Active' : type.charAt(0) + type.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      {/* Sheets Grid */}
      {filteredSheets.length === 0 ? (
        <Card className="p-12 text-center bg-gray-50 dark:bg-slate-800/50">
          <div className="bg-gray-200 dark:bg-slate-700 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
            <CheckCircle2 className="w-8 h-8 text-gray-400 dark:text-slate-500" />
          </div>
          <h3 className="text-xl font-bold dark:text-white mb-2">No task sheets found</h3>
          <p className="text-gray-500 dark:text-gray-400 text-sm">
            {searchQuery ? "No sheets match your search query." : "Check back later for new group tasks."}
          </p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          {filteredSheets.map((sheet, idx) => {
            const totalTasks = sheet.tasks.length;
            const completedTasks = sheet.tasks.filter((t) => t.completed).length;
            const progress = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
            
            const startDate = new Date(sheet.startDate).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
            const endDate = new Date(sheet.endDate).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
            
            const now = new Date();
            const isActive = new Date(sheet.startDate) <= now && new Date(sheet.endDate) >= now;

            return (
              <motion.div
                key={sheet.id}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3, delay: idx * 0.05 }}
                whileHover={{ y: -5 }}
                className="h-full"
              >
                <Card 
                  onClick={() => navigate(`/tasks/${sheet.id}`)}
                  className="flex flex-col h-full overflow-hidden border border-slate-200/80 dark:border-slate-800 shadow-sm hover:shadow-xl dark:hover:border-slate-700 transition-all cursor-pointer group"
                >
                  <div className="p-6 flex-1 flex flex-col">
                    <div className="flex justify-between items-start mb-3">
                      <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300 font-bold">
                        {sheet.sheetType}
                      </Badge>
                      {isActive && (
                        <span className="flex items-center gap-1.5 text-xs font-bold text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/40 px-2.5 py-0.5 rounded-full border border-emerald-200/50 dark:border-emerald-800/40">
                          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                          Active Now
                        </span>
                      )}
                    </div>
                    
                    <h3 className="text-xl font-black text-gray-900 dark:text-white mb-2 line-clamp-1 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                      {sheet.title}
                    </h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400 line-clamp-2 mb-4 flex-1">
                      {sheet.description}
                    </p>
                    
                    <div className="flex items-center gap-2 text-xs text-gray-500 dark:text-gray-400 mb-5">
                      <Calendar className="w-3.5 h-3.5 text-blue-500" />
                      <span>{startDate} — {endDate}</span>
                    </div>
                    
                    <div className="mt-auto pt-3 border-t border-gray-100 dark:border-slate-800">
                      <div className="flex justify-between text-xs font-bold mb-1.5">
                        <span className="text-gray-700 dark:text-gray-300">Solved</span>
                        <span className="text-emerald-600 dark:text-emerald-400 font-black">{completedTasks}/{totalTasks} ({progress}%)</span>
                      </div>
                      <div className="w-full bg-gray-200/70 dark:bg-slate-700/70 rounded-full h-2 overflow-hidden">
                        <motion.div 
                          initial={{ width: 0 }}
                          animate={{ width: `${progress}%` }}
                          transition={{ duration: 0.6, ease: 'easeOut' }}
                          className="bg-gradient-to-r from-emerald-500 to-teal-400 h-full rounded-full" 
                        />
                      </div>
                    </div>
                  </div>
                  
                  <div className="px-6 py-3 bg-gray-50/70 dark:bg-slate-850/70 border-t border-gray-100 dark:border-slate-800 flex justify-between items-center text-xs font-bold text-blue-600 dark:text-blue-400">
                    <span>Explore Problems</span>
                    <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                  </div>
                </Card>
              </motion.div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default TasksPage;
