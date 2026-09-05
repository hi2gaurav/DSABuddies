import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../lib/api';
import { User, TopicProgress } from '../types';
import { useAuth } from '../hooks/useAuth';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ActivityHeatmap from '../components/profile/ActivityHeatmap';
import TopicProgressCard from '../components/profile/TopicProgressCard';
import BadgesGrid from '../components/profile/BadgesGrid';
import AnimatedNumber from '../components/common/AnimatedNumber';
import { useToast } from '../components/ui/Toast';
import { motion } from 'framer-motion';
import {
  Calendar, Flame, Star, Trophy, Target, Zap, Shield,
  TrendingUp, CheckCircle2, Award
} from 'lucide-react';

const LEVEL_THRESHOLDS = [
  { level: 1, title: 'Novice', xp: 0 },
  { level: 2, title: 'Apprentice', xp: 100 },
  { level: 3, title: 'Code Scout', xp: 250 },
  { level: 4, title: 'Problem Solver', xp: 500 },
  { level: 5, title: 'Algorithm Knight', xp: 800 },
  { level: 6, title: 'Bug Slayer', xp: 1200 },
  { level: 7, title: 'Data Structurer', xp: 1700 },
  { level: 8, title: 'Pattern Seeker', xp: 2300 },
  { level: 9, title: 'Algorithm Ninja', xp: 3000 },
  { level: 10, title: 'Code Master', xp: 4000 },
  { level: 12, title: 'Senior Architect', xp: 5500 },
  { level: 15, title: 'System Architect', xp: 7500 },
  { level: 20, title: 'DSA Grandmaster', xp: 10000 },
  { level: 25, title: 'Interview Legend', xp: 15000 },
  { level: 30, title: 'Coding Deity', xp: 25000 },
];

const ProfilePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [profile, setProfile] = useState<User | null>(null);
  const [topics, setTopics] = useState<TopicProgress[]>([]);
  const [activity, setActivity] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);
  const [savingSettings, setSavingSettings] = useState(false);
  const { show } = useToast();
  
  const { user: currentUser } = useAuth();
  
  const profileId = id ? Number(id) : currentUser?.id;
  const isOwnProfile = !id || Number(id) === currentUser?.id;

  const fetchProfileData = async () => {
    if (!profileId) return;
    
    setLoading(true);
    try {
      const [profileData, topicsData, activityData] = await Promise.all([
        api.getProfile(id ? profileId : undefined),
        api.getTopicProgress(profileId),
        api.getActivity(profileId)
      ]);
      
      setProfile(profileData);
      setTopics(topicsData);
      setActivity(activityData);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfileData();
  }, [profileId, id]);

  const handleUpdateDailyGoal = async (newGoal: number) => {
    if (newGoal < 1 || newGoal > 20) return;
    setSavingSettings(true);
    try {
      const updated = await api.updateSettings({ dailyGoal: newGoal });
      setProfile(updated);
      show(`Daily goal updated to ${newGoal} problems/day`, 'success');
    } catch (err) {
      show('Failed to update daily goal', 'error');
    } finally {
      setSavingSettings(false);
    }
  };

  const handleUseStreakFreeze = async () => {
    if (!profile?.streakFreezeAvailable) return;
    setSavingSettings(true);
    try {
      const updated = await api.updateSettings({ useStreakFreeze: true });
      setProfile(updated);
      show('Streak Freeze activated! Your streak is saved.', 'success');
    } catch (err) {
      show('Failed to activate streak freeze', 'error');
    } finally {
      setSavingSettings(false);
    }
  };

  if (loading) return <div className="py-20"><LoadingSpinner size="lg" /></div>;
  if (!profile) return <div className="text-center py-20 text-gray-500">Profile not found.</div>;

  const joinDate = new Date(profile.createdAt).toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
  const totalSolved = topics.reduce((sum, t) => sum + t.completed, 0);

  // Level & XP math
  const currentLevel = profile.level || 1;
  const currentTitle = profile.title || 'Novice';
  const currentXp = profile.totalXp;
  
  const currentLevelInfo = LEVEL_THRESHOLDS.find(l => l.level === currentLevel) || LEVEL_THRESHOLDS[0];
  const nextLevelInfo = LEVEL_THRESHOLDS.find(l => l.xp > currentXp) || { level: currentLevel + 1, title: 'Legend', xp: currentXp + 500 };
  
  const xpInCurrentLevel = Math.max(0, currentXp - currentLevelInfo.xp);
  const xpNeededForNext = Math.max(1, nextLevelInfo.xp - currentLevelInfo.xp);
  const levelProgress = Math.min(100, Math.round((xpInCurrentLevel / xpNeededForNext) * 100));

  return (
    <div className="space-y-8 max-w-5xl mx-auto">
      {/* Profile Header */}
      <Card className="overflow-hidden border-0 shadow-lg relative">
        <div className="h-32 bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-600"></div>
        <div className="px-6 pb-6 relative">
          <div className="flex flex-col sm:flex-row items-center sm:items-end gap-6 -mt-16 sm:-mt-12 mb-4">
            <img 
              src={profile.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(profile.name)}`} 
              alt={profile.name} 
              className="w-28 h-28 sm:w-32 sm:h-32 rounded-full border-4 border-white dark:border-slate-800 shadow-md bg-white object-cover flex-shrink-0"
            />
            <div className="text-center sm:text-left flex-1">
              <h1 className="text-2xl sm:text-3xl font-bold dark:text-white flex items-center justify-center sm:justify-start gap-3">
                {profile.name}
                {profile.role === 'ROLE_ADMIN' && (
                  <Badge variant="admin">Admin</Badge>
                )}
              </h1>
              
              {/* Level & Title Pill */}
              <div className="flex items-center justify-center sm:justify-start gap-2 mt-1.5 flex-wrap">
                <span className="inline-flex items-center gap-1.5 px-3 py-0.5 rounded-full text-xs font-black bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-xs">
                  <Zap className="w-3 h-3 fill-current" />
                  Lv. {currentLevel} • {currentTitle}
                </span>
                {profile.email && (
                  <span className="text-gray-500 dark:text-gray-400 text-xs">{profile.email}</span>
                )}
              </div>
            </div>
            
            <div className="flex flex-col sm:flex-row items-center gap-3">
              <div className="flex items-center gap-2 text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-slate-700/50 px-3.5 py-1.5 rounded-lg text-xs">
                <Calendar className="w-3.5 h-3.5" /> Joined {joinDate}
              </div>
            </div>
          </div>

          {/* Level Progress Bar */}
          <div className="mt-4 pt-4 border-t border-gray-100 dark:border-slate-800">
            <div className="flex items-center justify-between text-xs font-bold mb-1.5">
              <span className="text-gray-600 dark:text-gray-300 flex items-center gap-1.5">
                <Award className="w-4 h-4 text-blue-500" />
                Level {currentLevel} ({currentXp} XP)
              </span>
              <span className="text-gray-500 dark:text-gray-400">
                {xpNeededForNext - xpInCurrentLevel} XP to Level {nextLevelInfo.level} ({nextLevelInfo.title})
              </span>
            </div>
            <div className="w-full bg-gray-200 dark:bg-slate-700 rounded-full h-2.5 overflow-hidden">
              <motion.div
                initial={{ width: 0 }}
                animate={{ width: `${levelProgress}%` }}
                transition={{ duration: 0.8, ease: 'easeOut' }}
                className="bg-gradient-to-r from-blue-600 to-indigo-500 h-full rounded-full"
              />
            </div>
          </div>
        </div>
      </Card>

      {/* Stats Overview */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <motion.div whileHover={{ y: -4 }} transition={{ duration: 0.2 }}>
          <Card className="p-5 text-center bg-gradient-to-br from-white to-amber-50/40 dark:from-slate-800 dark:to-slate-850 border border-amber-100 dark:border-slate-700/60 shadow-xs h-full">
            <Star className="w-7 h-7 text-amber-500 mx-auto mb-1.5 fill-amber-500/20" />
            <h3 className="text-3xl font-black text-gray-900 dark:text-white">
              <AnimatedNumber value={profile.totalXp} />
            </h3>
            <p className="text-xs font-bold text-gray-500 dark:text-gray-400 mt-0.5 uppercase tracking-wider">Total XP</p>
          </Card>
        </motion.div>
        
        <motion.div whileHover={{ y: -4 }} transition={{ duration: 0.2 }}>
          <Card className="p-5 text-center bg-gradient-to-br from-white to-orange-50/40 dark:from-slate-800 dark:to-slate-850 border border-orange-100 dark:border-slate-700/60 shadow-xs h-full">
            <Flame className="w-7 h-7 text-orange-500 mx-auto mb-1.5 fill-orange-500/20" />
            <h3 className="text-3xl font-black text-gray-900 dark:text-white">
              <AnimatedNumber value={profile.currentStreak} />
            </h3>
            <p className="text-xs font-bold text-gray-500 dark:text-gray-400 mt-0.5 uppercase tracking-wider">Current Streak</p>
          </Card>
        </motion.div>
        
        <motion.div whileHover={{ y: -4 }} transition={{ duration: 0.2 }}>
          <Card className="p-5 text-center bg-gradient-to-br from-white to-blue-50/40 dark:from-slate-800 dark:to-slate-850 border border-blue-100 dark:border-slate-700/60 shadow-xs h-full">
            <Trophy className="w-7 h-7 text-blue-500 mx-auto mb-1.5" />
            <h3 className="text-3xl font-black text-gray-900 dark:text-white">
              <AnimatedNumber value={profile.maxStreak} />
            </h3>
            <p className="text-xs font-bold text-gray-500 dark:text-gray-400 mt-0.5 uppercase tracking-wider">Best Streak</p>
          </Card>
        </motion.div>
        
        <motion.div whileHover={{ y: -4 }} transition={{ duration: 0.2 }}>
          <Card className="p-5 text-center bg-gradient-to-br from-white to-emerald-50/40 dark:from-slate-800 dark:to-slate-850 border border-emerald-100 dark:border-slate-700/60 shadow-xs h-full">
            <Target className="w-7 h-7 text-emerald-500 mx-auto mb-1.5" />
            <h3 className="text-3xl font-black text-gray-900 dark:text-white">
              <AnimatedNumber value={totalSolved} />
            </h3>
            <p className="text-xs font-bold text-gray-500 dark:text-gray-400 mt-0.5 uppercase tracking-wider">Problems Solved</p>
          </Card>
        </motion.div>
      </div>

      {/* Gamification Settings & Consistency Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* 30-Day Consistency Score */}
        <Card className="p-5 flex items-center gap-4 bg-white dark:bg-slate-800/80">
          <div className="relative w-14 h-14 flex items-center justify-center flex-shrink-0">
            <svg className="w-full h-full -rotate-90" viewBox="0 0 36 36">
              <path
                className="text-gray-200 dark:text-slate-700"
                strokeWidth="3.5"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
              <path
                className="text-emerald-500 transition-all duration-700"
                strokeDasharray={`${profile.consistencyScore || 0}, 100`}
                strokeWidth="3.5"
                strokeLinecap="round"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
            </svg>
            <span className="absolute text-xs font-black text-gray-900 dark:text-white">
              {Math.round(profile.consistencyScore || 0)}%
            </span>
          </div>
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 flex items-center gap-1">
              <TrendingUp className="w-3.5 h-3.5 text-emerald-500" />
              Consistency Score
            </h4>
            <p className="text-xs text-gray-600 dark:text-gray-300 mt-0.5">
              Based on active problem-solving days over the last 30 days.
            </p>
          </div>
        </Card>

        {/* Streak Freeze */}
        <Card className="p-5 flex items-center justify-between gap-4 bg-white dark:bg-slate-800/80">
          <div className="flex items-center gap-3">
            <div className={`w-11 h-11 rounded-2xl flex items-center justify-center flex-shrink-0 ${
              profile.streakFreezeAvailable
                ? 'bg-blue-100 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400'
                : 'bg-gray-100 dark:bg-slate-700 text-gray-400'
            }`}>
              <Shield className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-gray-900 dark:text-white">
                Streak Freeze
              </h4>
              <p className="text-[11px] text-gray-500 dark:text-gray-400">
                {profile.streakFreezeAvailable
                  ? 'Active & Ready (Protects 1 missed day)'
                  : profile.streakFreezeUsedDate
                  ? `Used on ${profile.streakFreezeUsedDate}`
                  : 'Unlocks at Level 5'}
              </p>
            </div>
          </div>
          {isOwnProfile && profile.streakFreezeAvailable && (
            <button
              onClick={handleUseStreakFreeze}
              disabled={savingSettings}
              className="px-3 py-1.5 rounded-xl text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white shadow-xs transition-all active:scale-95 flex-shrink-0"
            >
              Use Freeze
            </button>
          )}
        </Card>

        {/* Daily Problem Goal */}
        <Card className="p-5 flex items-center justify-between gap-4 bg-white dark:bg-slate-800/80">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-amber-100 dark:bg-amber-950/60 text-amber-600 dark:text-amber-400 flex items-center justify-center flex-shrink-0">
              <CheckCircle2 className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-gray-900 dark:text-white">
                Daily Goal
              </h4>
              <p className="text-[11px] text-gray-500 dark:text-gray-400">
                Target: <span className="font-bold text-gray-900 dark:text-white">{profile.dailyGoal || 3}</span> problems/day
              </p>
            </div>
          </div>
          {isOwnProfile && (
            <div className="flex items-center gap-1.5 flex-shrink-0">
              <button
                onClick={() => handleUpdateDailyGoal((profile.dailyGoal || 3) - 1)}
                disabled={savingSettings || (profile.dailyGoal || 3) <= 1}
                className="w-7 h-7 rounded-lg bg-gray-100 dark:bg-slate-700 text-gray-700 dark:text-gray-200 font-bold hover:bg-gray-200 flex items-center justify-center disabled:opacity-40"
              >
                -
              </button>
              <span className="text-xs font-extrabold w-4 text-center">
                {profile.dailyGoal || 3}
              </span>
              <button
                onClick={() => handleUpdateDailyGoal((profile.dailyGoal || 3) + 1)}
                disabled={savingSettings || (profile.dailyGoal || 3) >= 20}
                className="w-7 h-7 rounded-lg bg-gray-100 dark:bg-slate-700 text-gray-700 dark:text-gray-200 font-bold hover:bg-gray-200 flex items-center justify-center disabled:opacity-40"
              >
                +
              </button>
            </div>
          )}
        </Card>
      </div>

      {/* Dynamic Badges Gallery */}
      <Card className="p-6">
        <h3 className="text-lg font-bold dark:text-white mb-4 flex items-center gap-2">
          <Award className="w-5 h-5 text-amber-500" /> Badges & Achievements
        </h3>
        <BadgesGrid userId={profile.id} />
      </Card>

      {/* Activity Heatmap */}
      <Card className="p-6">
        <h3 className="text-lg font-bold dark:text-white mb-6 flex items-center gap-2">
          <Calendar className="w-5 h-5 text-blue-500" /> Activity History
        </h3>
        <div className="overflow-x-auto pb-4">
          <ActivityHeatmap data={activity} />
        </div>
      </Card>

      {/* Topic Progress */}
      <div>
        <h3 className="text-xl font-bold dark:text-white mb-4">Topic Mastery</h3>
        {topics.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {topics.map(topic => (
              <TopicProgressCard key={topic.topicName} topic={topic} />
            ))}
          </div>
        ) : (
          <Card className="p-8 text-center text-gray-500 dark:text-gray-400">
            No topic data available yet. Start solving problems!
          </Card>
        )}
      </div>
    </div>
  );
};

export default ProfilePage;
