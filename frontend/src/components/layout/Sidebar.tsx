import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, ListTodo, Trophy, Settings, Code, Bookmark as BookmarkIcon, Timer } from 'lucide-react';
import { motion } from 'framer-motion';
import { useAuth } from '../../hooks/useAuth';
import { clsx } from 'clsx';

interface SidebarProps {
  isOpen: boolean;
  setIsOpen: (isOpen: boolean) => void;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen, setIsOpen }) => {
  const { user } = useAuth();

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Tasks', path: '/tasks', icon: ListTodo },
    { name: 'Mock Interview', path: '/mock-interview', icon: Timer },
    { name: 'Bookmarks', path: '/bookmarks', icon: BookmarkIcon },
    { name: 'Leaderboard', path: '/leaderboard', icon: Trophy },
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
        "fixed top-0 left-0 h-full w-64 glass dark:bg-slate-900/90 border-r border-gray-200/80 dark:border-slate-800/80 z-40 transform transition-transform duration-300 ease-[cubic-bezier(0.16,1,0.3,1)] md:translate-x-0 shadow-lg md:shadow-none flex flex-col",
        isOpen ? "translate-x-0" : "-translate-x-full"
      )}>
        {/* Brand Header */}
        <div className="p-6 flex items-center justify-between flex-shrink-0">
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

        {/* Navigation Items */}
        <nav className="mt-2 px-3 space-y-1.5 flex-1 overflow-y-auto">
          {navItems.map((item) => (
            <NavLink
              key={item.name}
              to={item.path}
              onClick={() => setIsOpen(false)}
              className={({ isActive }) => clsx(
                "group relative flex items-center gap-3 px-4 py-3 rounded-xl font-bold text-xs tracking-wide transition-all duration-200",
                isActive 
                  ? "bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-md shadow-blue-500/25" 
                  : item.path === '/admin'
                  ? "text-blue-600 dark:text-blue-400 bg-blue-50/50 dark:bg-blue-950/20 hover:bg-blue-100/60 dark:hover:bg-blue-900/30"
                  : "text-gray-600 dark:text-gray-400 hover:bg-gray-100/80 dark:hover:bg-slate-800/60 hover:text-gray-900 dark:hover:text-white"
              )}
            >
              {({ isActive }) => (
                <>
                  <item.icon className={clsx(
                    "w-4 h-4 transition-transform group-hover:scale-110 duration-200",
                    isActive ? "text-white" : item.path === '/admin' ? "text-blue-500" : "text-gray-400 group-hover:text-blue-500 dark:group-hover:text-blue-400"
                  )} />
                  <span className="flex-1">{item.name}</span>
                  {item.path === '/admin' && !isActive && (
                    <span className="px-1.5 py-0.5 rounded text-[9px] font-black uppercase tracking-wider bg-blue-100 text-blue-700 dark:bg-blue-900/50 dark:text-blue-300">
                      Admin
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
      </aside>
    </>
  );
};

export default Sidebar;
