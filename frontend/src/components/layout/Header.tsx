import React from 'react';
import { Menu, Sun, Moon, Bell, LogOut, User as UserIcon } from 'lucide-react';
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
    <header className="sticky top-0 z-10 bg-white/80 dark:bg-slate-900/80 backdrop-blur-md border-b border-gray-200 dark:border-slate-700">
      <div className="flex items-center justify-between px-4 sm:px-6 py-4">
        <div className="flex items-center gap-4">
          <button 
            onClick={toggleSidebar}
            className="p-2 -ml-2 text-gray-500 hover:bg-gray-100 dark:hover:bg-slate-800 rounded-lg md:hidden"
          >
            <Menu className="w-6 h-6" />
          </button>
          <h1 className="text-xl font-semibold dark:text-white hidden sm:block">
            {getPageTitle()}
          </h1>
        </div>

        <div className="flex items-center gap-3 sm:gap-4">
          <button 
            onClick={toggleTheme}
            className="p-2 text-gray-500 hover:bg-gray-100 dark:hover:bg-slate-800 rounded-full transition-colors"
          >
            {theme === 'dark' ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
          </button>
          
          <button className="p-2 text-gray-500 hover:bg-gray-100 dark:hover:bg-slate-800 rounded-full transition-colors relative">
            <Bell className="w-5 h-5" />
            <span className="absolute top-2 right-2 w-2 h-2 bg-red-500 rounded-full"></span>
          </button>

          <div className="relative">
            <button 
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center gap-2 p-1 pl-2 pr-3 bg-gray-100 dark:bg-slate-800 rounded-full hover:bg-gray-200 dark:hover:bg-slate-700 transition-colors"
            >
              <img 
                src={user?.avatarUrl || `https://ui-avatars.com/api/?name=${user?.name || 'User'}`} 
                alt="Avatar" 
                className="w-8 h-8 rounded-full bg-blue-500"
              />
              <span className="text-sm font-medium dark:text-white hidden sm:block">
                {user?.name.split(' ')[0]}
              </span>
            </button>

            {dropdownOpen && (
              <>
                <div 
                  className="fixed inset-0 z-10" 
                  onClick={() => setDropdownOpen(false)}
                />
                <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-slate-800 rounded-xl shadow-lg border border-gray-200 dark:border-slate-700 z-20 py-1 overflow-hidden">
                  <div className="px-4 py-2 border-b border-gray-100 dark:border-slate-700 sm:hidden">
                    <p className="text-sm font-medium dark:text-white">{user?.name}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400 truncate">{user?.email}</p>
                  </div>
                  <button 
                    onClick={() => { navigate('/profile'); setDropdownOpen(false); }}
                    className="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-slate-700 flex items-center gap-2"
                  >
                    <UserIcon className="w-4 h-4" /> Profile
                  </button>
                  <button 
                    onClick={() => { logout(); setDropdownOpen(false); }}
                    className="w-full text-left px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 flex items-center gap-2"
                  >
                    <LogOut className="w-4 h-4" /> Sign out
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
