import React from 'react';
import { Menu, Sun, Moon, Bell, LogOut, User as UserIcon, Flame } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { useTheme } from '../../hooks/useTheme';
import { useAuth } from '../../hooks/useAuth';
import { useNavigate, useLocation } from 'react-router-dom';

interface HeaderProps {
  toggleSidebar: () => void;
}

const Header: React.FC<HeaderProps> = ({ toggleSidebar }) => {
  const { theme, toggleTheme } = useTheme();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [dropdownOpen, setDropdownOpen] = React.useState(false);
  const [notifOpen, setNotifOpen] = React.useState(false);
  const [announcements, setAnnouncements] = React.useState<import('../../types').Announcement[]>([]);
  const [dismissedIds, setDismissedIds] = React.useState<number[]>(() => {
    try {
      return JSON.parse(localStorage.getItem('dsa_buddies_dismissed_announcements') || '[]');
    } catch {
      return [];
    }
  });

  React.useEffect(() => {
    import('../../lib/api').then(({ api }) => {
      api.getActiveAnnouncements()
        .then((data) => setAnnouncements(data || []))
        .catch(() => setAnnouncements([]));
    });
  }, []);

  const unreadAnnouncements = announcements.filter((a) => !dismissedIds.includes(a.id));
  const hasUnread = unreadAnnouncements.length > 0;

  const markAsRead = (id: number) => {
    const updated = [...dismissedIds, id];
    setDismissedIds(updated);
    localStorage.setItem('dsa_buddies_dismissed_announcements', JSON.stringify(updated));
  };

  const markAllAsRead = () => {
    const allIds = announcements.map((a) => a.id);
    const updated = Array.from(new Set([...dismissedIds, ...allIds]));
    setDismissedIds(updated);
    localStorage.setItem('dsa_buddies_dismissed_announcements', JSON.stringify(updated));
  };

  const getPageTitle = () => {
    const path = location.pathname;
    if (path === '/dashboard') return 'Dashboard';
    if (path.startsWith('/tasks')) return 'Task Sheets';
    if (path === '/leaderboard') return 'Leaderboard';
    if (path.startsWith('/profile')) return 'Profile';
    if (path === '/admin') return 'Administration';
    return '';
  };

  return (
    <header className="sticky top-0 z-20 glass border-b border-gray-200/80 dark:border-slate-800/80 shadow-xs transition-colors">
      <div className="flex items-center justify-between px-4 sm:px-6 py-3.5">
        <div className="flex items-center gap-4">
          <button 
            onClick={toggleSidebar}
            className="p-2 -ml-2 text-gray-500 hover:bg-gray-100 dark:hover:bg-slate-800 rounded-xl md:hidden btn-press transition-colors"
          >
            <Menu className="w-5 h-5" />
          </button>
          <div className="hidden sm:flex items-center gap-2.5">
            <h1 className="text-xl font-black tracking-tight text-gray-900 dark:text-white">
              {getPageTitle()}
            </h1>
          </div>
        </div>

        <div className="flex items-center gap-2.5 sm:gap-4">
          {/* User Streak Badge */}
          {user && (
            <div 
              className="flex items-center gap-1.5 px-3 py-1.5 bg-orange-500/10 border border-orange-500/20 text-orange-600 dark:text-orange-400 rounded-full text-xs font-black shadow-xs cursor-default select-none"
            >
              <Flame className="w-4 h-4 fill-orange-500 text-orange-500" />
              <span>{user.currentStreak || 0} {user.currentStreak === 1 ? 'Day' : 'Days'}</span>
            </div>
          )}

          {/* Theme Toggle Button with Rotation Animation */}
          <motion.button 
            whileHover={{ scale: 1.08 }}
            whileTap={{ scale: 0.92, rotate: 180 }}
            onClick={toggleTheme}
            className="p-2.5 text-gray-500 hover:bg-gray-100/80 dark:hover:bg-slate-800/80 rounded-xl transition-colors border border-transparent hover:border-gray-200 dark:hover:border-slate-700"
            title={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
          >
            {theme === 'dark' ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-slate-700" />}
          </motion.button>
          
          {/* Notification Bell with Conditional Red Dot */}
          <div className="relative">
            <button 
              onClick={() => setNotifOpen(!notifOpen)}
              className="p-2.5 text-gray-500 hover:bg-gray-100/80 dark:hover:bg-slate-800/80 rounded-xl transition-colors relative border border-transparent hover:border-gray-200 dark:hover:border-slate-700 btn-press"
              title="Announcements"
            >
              <Bell className="w-4 h-4" />
              {hasUnread && (
                <span className="absolute top-2 right-2 w-2 h-2 bg-red-500 rounded-full ring-2 ring-white dark:ring-slate-900 animate-pulse"></span>
              )}
            </button>

            <AnimatePresence>
              {notifOpen && (
                <>
                  <div 
                    className="fixed inset-0 z-10" 
                    onClick={() => setNotifOpen(false)}
                  />
                  <motion.div 
                    initial={{ opacity: 0, scale: 0.95, y: -6 }}
                    animate={{ opacity: 1, scale: 1, y: 0 }}
                    exit={{ opacity: 0, scale: 0.95, y: -6 }}
                    transition={{ duration: 0.15, ease: 'easeOut' }}
                    className="absolute right-0 mt-2 w-80 glass-card rounded-2xl shadow-xl border border-gray-200/80 dark:border-slate-700/80 z-20 py-2 overflow-hidden"
                  >
                    <div className="px-4 py-2 border-b border-gray-100 dark:border-slate-700/80 flex items-center justify-between">
                      <div className="flex items-center gap-1.5">
                        <Bell className="w-3.5 h-3.5 text-indigo-500" />
                        <span className="text-xs font-bold text-gray-900 dark:text-white">Announcements</span>
                        {unreadAnnouncements.length > 0 && (
                          <span className="text-[10px] font-extrabold px-1.5 py-0.5 bg-red-100 dark:bg-red-950/60 text-red-600 dark:text-red-400 rounded-full">
                            {unreadAnnouncements.length} new
                          </span>
                        )}
                      </div>
                      {unreadAnnouncements.length > 0 && (
                        <button 
                          onClick={markAllAsRead}
                          className="text-[10px] text-blue-600 dark:text-blue-400 hover:underline font-semibold"
                        >
                          Mark all read
                        </button>
                      )}
                    </div>

                    <div className="max-h-64 overflow-y-auto divide-y divide-gray-100 dark:divide-slate-800">
                      {announcements.length === 0 ? (
                        <div className="p-4 text-center text-xs text-gray-400">
                          No active announcements at this time.
                        </div>
                      ) : (
                        announcements.map((a) => {
                          const isUnread = !dismissedIds.includes(a.id);
                          return (
                            <div key={a.id} className={`p-3 text-xs transition-colors ${isUnread ? 'bg-indigo-50/40 dark:bg-indigo-950/20' : ''}`}>
                              <div className="flex items-center justify-between gap-2 mb-1">
                                <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded uppercase tracking-wider ${
                                  a.priority === 'URGENT'
                                    ? 'bg-red-100 dark:bg-red-950/60 text-red-700 dark:text-red-400'
                                    : a.priority === 'HIGH'
                                    ? 'bg-amber-100 dark:bg-amber-950/60 text-amber-700 dark:text-amber-400'
                                    : 'bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-400'
                                }`}>
                                  {a.priority}
                                </span>
                                {isUnread && (
                                  <button
                                    onClick={() => markAsRead(a.id)}
                                    className="text-[10px] text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
                                  >
                                    Dismiss
                                  </button>
                                )}
                              </div>
                              <h4 className="font-semibold text-gray-900 dark:text-white">{a.title}</h4>
                              <p className="text-gray-600 dark:text-gray-300 text-[11px] mt-0.5 leading-relaxed">{a.message}</p>
                            </div>
                          );
                        })
                      )}
                    </div>
                  </motion.div>
                </>
              )}
            </AnimatePresence>
          </div>

          {/* User Profile Dropdown */}
          <div className="relative">
            <motion.button 
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center gap-2 p-1 pl-1.5 pr-3 bg-gray-100/80 dark:bg-slate-800/80 border border-gray-200/60 dark:border-slate-700/60 rounded-full hover:bg-gray-200/80 dark:hover:bg-slate-700 transition-all shadow-2xs"
            >
              <img 
                src={user?.avatarUrl || `https://ui-avatars.com/api/?name=${user?.name || 'User'}&background=3b82f6&color=fff`} 
                alt="Avatar" 
                className="w-7 h-7 rounded-full object-cover ring-2 ring-blue-500/30"
              />
              <span className="text-xs font-bold text-gray-800 dark:text-gray-200 hidden sm:block">
                {user?.name.split(' ')[0]}
              </span>
            </motion.button>

            <AnimatePresence>
              {dropdownOpen && (
                <>
                  <div 
                    className="fixed inset-0 z-10" 
                    onClick={() => setDropdownOpen(false)}
                  />
                  <motion.div 
                    initial={{ opacity: 0, scale: 0.95, y: -6 }}
                    animate={{ opacity: 1, scale: 1, y: 0 }}
                    exit={{ opacity: 0, scale: 0.95, y: -6 }}
                    transition={{ duration: 0.15, ease: 'easeOut' }}
                    className="absolute right-0 mt-2 w-52 glass-card rounded-2xl shadow-xl border border-gray-200/80 dark:border-slate-700/80 z-20 py-1.5 overflow-hidden"
                  >
                    <div className="px-4 py-2.5 border-b border-gray-100 dark:border-slate-700/80">
                      <p className="text-xs font-bold text-gray-900 dark:text-white truncate">{user?.name}</p>
                      <p className="text-[11px] text-gray-500 dark:text-gray-400 truncate mt-0.5">{user?.email}</p>
                      <span className="mt-1.5 inline-block text-[10px] font-extrabold px-2 py-0.5 rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300">
                        {user?.role === 'ROLE_ADMIN' ? '⚡ Administrator' : 'Member'}
                      </span>
                    </div>
                    <button 
                      onClick={() => { navigate('/profile'); setDropdownOpen(false); }}
                      className="w-full text-left px-4 py-2 text-xs font-semibold text-gray-700 dark:text-gray-300 hover:bg-blue-50/70 dark:hover:bg-slate-700/60 flex items-center gap-2.5 transition-colors"
                    >
                      <UserIcon className="w-3.5 h-3.5 text-blue-500" /> My Profile
                    </button>
                    <button 
                      onClick={() => { logout(); setDropdownOpen(false); }}
                      className="w-full text-left px-4 py-2 text-xs font-semibold text-red-600 dark:text-red-400 hover:bg-red-50/70 dark:hover:bg-red-900/20 flex items-center gap-2.5 transition-colors"
                    >
                      <LogOut className="w-3.5 h-3.5" /> Sign out
                    </button>
                  </motion.div>
                </>
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
