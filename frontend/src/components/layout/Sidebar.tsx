import React, { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, ListTodo, Trophy, UserCircle, Settings, Code, Brain, Bookmark as BookmarkIcon, Timer, Layers, BookOpen, Sparkles } from 'lucide-react';
import { motion } from 'framer-motion';
import { useAuth } from '../../hooks/useAuth';
import { api } from '../../lib/api';
import { clsx } from 'clsx';

interface SidebarProps {
  isOpen: boolean;
  setIsOpen: (isOpen: boolean) => void;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen, setIsOpen }) => {
  const { user } = useAuth();
  const [dueCount, setDueCount] = useState<number>(0);

  useEffect(() => {
    if (user) {
      api.getDueReviewCount()
        .then(res => setDueCount(res.dueCount))
        .catch(() => {});
    }
  }, [user]);

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Tasks', path: '/tasks', icon: ListTodo },
    { name: 'Mock Interview', path: '/mock-interview', icon: Timer },
    { name: 'System Design', path: '/designs', icon: Layers },
    { name: 'Flashcards', path: '/flashcards', icon: BookOpen },
    { name: 'Reviews', path: '/reviews', icon: Brain, badge: dueCount > 0 ? dueCount : undefined },
    { name: 'Bookmarks', path: '/bookmarks', icon: BookmarkIcon },
    { name: 'Leaderboard', path: '/leaderboard', icon: Trophy },
    { name: 'Profile', path: '/profile', icon: UserCircle },
  ];

  if (user?.role === 'ROLE_ADMIN') {
    navItems.push({ name: 'Admin Hub', path: '/admin', icon: Settings });
  }

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div 
          className="fixed inset-0 bg-black/60 backdrop-blur-xs z-30 md:hidden animate-fade-in"
          onClick={() => setIsOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={clsx(
        "fixed top-0 left-0 h-full w-64 glass dark:bg-slate-900/90 border-r border-gray-200/80 dark:border-slate-800/80 z-40 transform transition-transform duration-300 ease-[cubic-bezier(0.16,1,0.3,1)] md:translate-x-0 shadow-lg md:shadow-none",
        isOpen ? "translate-x-0" : "-translate-x-full"
      )}>
        <div className="p-6 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="bg-gradient-to-tr from-blue-600 via-indigo-600 to-blue-500 p-2.5 rounded-xl shadow-md shadow-blue-500/25">
              <Code className="text-white w-5 h-5" />
            </div>
            <div>
              <span className="text-lg font-black tracking-tight text-gray-900 dark:text-white block">DSA Buddies</span>
              <span className="text-[10px] uppercase font-extrabold tracking-wider text-blue-600 dark:text-blue-400">Prep Tracker</span>
            </div>
          </div>
        </div>

        <nav className="mt-4 px-3 space-y-1.5">
          {navItems.map((item) => (
            <NavLink
              key={item.name}
              to={item.path}
              onClick={() => setIsOpen(false)}
              className={({ isActive }) => clsx(
                "group relative flex items-center gap-3 px-4 py-3 rounded-xl font-bold text-xs tracking-wide transition-all duration-200",
                isActive 
                  ? "bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-md shadow-blue-500/25" 
                  : "text-gray-600 dark:text-gray-400 hover:bg-gray-100/80 dark:hover:bg-slate-800/60 hover:text-gray-900 dark:hover:text-white"
              )}
            >
              {({ isActive }) => (
                <>
                  <item.icon className={clsx(
                    "w-4 h-4 transition-transform group-hover:scale-110 duration-200",
                    isActive ? "text-white" : "text-gray-400 group-hover:text-blue-500 dark:group-hover:text-blue-400"
                  )} />
                  <span className="flex-1">{item.name}</span>
                  {item.badge !== undefined && (
                    <span className={clsx(
                      "px-2 py-0.5 rounded-full text-[10px] font-black",
                      isActive
                        ? "bg-white/20 text-white"
                        : "bg-purple-100 text-purple-700 dark:bg-purple-900/60 dark:text-purple-300 animate-pulse"
                    )}>
                      {item.badge}
                    </span>
                  )}
                  {isActive && (
                    <motion.div
                      layoutId="activePill"
                      className="w-1.5 h-1.5 rounded-full bg-white shadow-xs"
                      transition={{ type: 'spring', stiffness: 400, damping: 30 }}
                    />
                  )}
                </>
              )}
            </NavLink>
          ))}
        </nav>

        {/* Bottom motivational card */}
        <div className="absolute bottom-6 left-3 right-3 p-4 rounded-2xl bg-gradient-to-br from-blue-500/10 via-indigo-500/5 to-purple-500/10 border border-blue-500/20 dark:border-blue-500/10">
          <div className="flex items-center gap-2 text-blue-600 dark:text-blue-400 text-xs font-bold mb-1">
            <Sparkles className="w-3.5 h-3.5 fill-current" />
            <span>Consistency is Key</span>
          </div>
          <p className="text-[11px] text-gray-500 dark:text-gray-400 leading-relaxed">
            Solve just 1 problem every day. Small efforts compound into big offers.
          </p>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
