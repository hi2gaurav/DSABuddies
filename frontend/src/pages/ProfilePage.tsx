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
import AchievementsList from '../components/profile/AchievementsList';
import AnimatedNumber from '../components/common/AnimatedNumber';
import { motion } from 'framer-motion';
import { Calendar, Flame, Star, Trophy, Target } from 'lucide-react';

const ProfilePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [profile, setProfile] = useState<User | null>(null);
  const [topics, setTopics] = useState<TopicProgress[]>([]);
  const [activity, setActivity] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);
  
  const { user: currentUser } = useAuth();
  
  const profileId = id ? Number(id) : currentUser?.id;

  useEffect(() => {
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
    
    fetchProfileData();
  }, [profileId, id]);

  if (loading) return <div className="py-20"><LoadingSpinner size="lg" /></div>;
  if (!profile) return <div className="text-center py-20 text-gray-500">Profile not found.</div>;

  const joinDate = new Date(profile.createdAt).toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
  const totalSolved = topics.reduce((sum, t) => sum + t.completed, 0);

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
              {profile.email && (
                <p className="text-gray-500 dark:text-gray-400 text-sm mt-0.5">{profile.email}</p>
              )}
            </div>
            
            <div className="flex flex-col sm:flex-row items-center gap-3">
              <div className="flex items-center gap-2 text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-slate-700/50 px-3.5 py-1.5 rounded-lg text-xs">
                <Calendar className="w-3.5 h-3.5" /> Joined {joinDate}
              </div>
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

      {/* Milestone Achievements */}
      <AchievementsList user={profile} problemsSolved={totalSolved} />

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
