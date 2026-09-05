import React from 'react';
import { useAuth } from '../hooks/useAuth';
import { Navigate } from 'react-router-dom';
import { Code, TrendingUp, Trophy, Users, Sparkles } from 'lucide-react';
import { motion } from 'framer-motion';

const LoginPage: React.FC = () => {
  const { login, authenticated } = useAuth();

  if (authenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4 relative overflow-hidden">
      {/* Dynamic ambient floating glowing orbs */}
      <motion.div 
        animate={{
          scale: [1, 1.25, 1],
          x: [0, 40, 0],
          y: [0, -30, 0],
        }}
        transition={{ duration: 12, repeat: Infinity, ease: 'easeInOut' }}
        className="absolute top-[-10%] left-[-5%] w-[45vw] h-[45vw] rounded-full bg-blue-600/20 blur-[130px] pointer-events-none" 
      />
      <motion.div 
        animate={{
          scale: [1.2, 1, 1.2],
          x: [0, -50, 0],
          y: [0, 40, 0],
        }}
        transition={{ duration: 14, repeat: Infinity, ease: 'easeInOut' }}
        className="absolute bottom-[-10%] right-[-5%] w-[45vw] h-[45vw] rounded-full bg-emerald-500/15 blur-[140px] pointer-events-none" 
      />
      <div className="absolute top-[40%] right-[15%] w-[30vw] h-[30vw] rounded-full bg-purple-600/15 blur-[120px] pointer-events-none" />
      
      <motion.div 
        initial={{ opacity: 0, scale: 0.94, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
        className="max-w-md w-full glass-card bg-slate-900/80 backdrop-blur-2xl border border-slate-700/60 rounded-3xl shadow-2xl p-8 sm:p-10 z-10 relative overflow-hidden"
      >
        <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-blue-500 via-indigo-500 to-emerald-500" />
        
        <div className="flex flex-col items-center mb-8">
          <motion.div 
            whileHover={{ rotate: 10, scale: 1.1 }}
            className="bg-gradient-to-tr from-blue-600 via-indigo-600 to-blue-500 p-4 rounded-2xl mb-5 shadow-xl shadow-blue-500/30"
          >
            <Code className="w-9 h-9 text-white" />
          </motion.div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-400 text-xs font-bold mb-2">
            <Sparkles className="w-3.5 h-3.5 text-blue-400" /> WhatsApp Study Group
          </div>
          <h1 className="text-3xl font-black text-white tracking-tight text-center">DSA Buddies</h1>
          <p className="text-slate-400 text-center text-sm mt-1 max-w-xs">
            Daily DSA challenge tracking, interview questions & community leaderboards
          </p>
        </div>

        <motion.button 
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          onClick={login}
          className="w-full bg-white text-gray-900 hover:bg-gray-50 flex items-center justify-center gap-3 py-3.5 px-5 rounded-2xl font-bold text-sm transition-all shadow-lg shadow-white/10"
        >
          <svg className="w-5 h-5" viewBox="0 0 24 24">
            <path
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              fill="#4285F4"
            />
            <path
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              fill="#34A853"
            />
            <path
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              fill="#FBBC05"
            />
            <path
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              fill="#EA4335"
            />
          </svg>
          Continue with Google
        </motion.button>

        <div className="mt-8 space-y-2.5">
          <div className="flex items-center gap-3.5 text-slate-300 bg-slate-800/40 p-3 rounded-xl border border-slate-700/40 text-xs">
            <div className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-400">
              <TrendingUp className="w-4 h-4" />
            </div>
            <span className="font-medium">Track daily problem-solving progress & streaks</span>
          </div>
          <div className="flex items-center gap-3.5 text-slate-300 bg-slate-800/40 p-3 rounded-xl border border-slate-700/40 text-xs">
            <div className="p-1.5 rounded-lg bg-amber-500/10 text-amber-400">
              <Trophy className="w-4 h-4" />
            </div>
            <span className="font-medium">Compete on real-time community leaderboards</span>
          </div>
          <div className="flex items-center gap-3.5 text-slate-300 bg-slate-800/40 p-3 rounded-xl border border-slate-700/40 text-xs">
            <div className="p-1.5 rounded-lg bg-blue-500/10 text-blue-400">
              <Users className="w-4 h-4" />
            </div>
            <span className="font-medium">Daily curated Java, Spring Boot & System Design questions</span>
          </div>
        </div>
      </motion.div>
    </div>
  );
};

export default LoginPage;
