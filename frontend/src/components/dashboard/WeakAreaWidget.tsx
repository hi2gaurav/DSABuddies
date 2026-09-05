import React, { useEffect, useState } from 'react';
import { AlertCircle, Target, Sparkles, TrendingUp, ChevronRight } from 'lucide-react';
import { motion } from 'framer-motion';
import { api } from '../../lib/api';
import { WeakTopic, AdaptiveSuggestion, PatternStat } from '../../types';
import Card from '../ui/Card';
import Badge from '../ui/Badge';
import { toSafeUrl } from '../../lib/security';

export const WeakAreaWidget: React.FC = () => {
  const [weakTopics, setWeakTopics] = useState<WeakTopic[]>([]);
  const [suggestions, setSuggestions] = useState<AdaptiveSuggestion[]>([]);
  const [patterns, setPatterns] = useState<PatternStat[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.getWeakTopics().catch(() => []),
      api.getAdaptiveSuggestions().catch(() => []),
      api.getPatternStats().catch(() => []),
    ]).then(([wt, sug, pat]) => {
      setWeakTopics(wt);
      setSuggestions(sug);
      setPatterns(pat);
    }).finally(() => setLoading(false));
  }, []);

  if (loading) return null;
  if (weakTopics.length === 0 && suggestions.length === 0) return null;

  return (
    <motion.div
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, ease: 'easeOut' }}
      className="space-y-6"
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="p-2 rounded-xl bg-rose-500/10 text-rose-500 border border-rose-500/20">
            <AlertCircle className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-xl font-black text-gray-900 dark:text-white tracking-tight flex items-center gap-2">
              <span>Weak-Area Detection & Recommendations</span>
              <span className="text-[10px] uppercase font-extrabold px-2 py-0.5 rounded-full bg-rose-100 text-rose-700 dark:bg-rose-950/40 dark:text-rose-400">
                Adaptive
              </span>
            </h2>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Personalized insights based on your completion rates and confidence ratings
            </p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Weak Topics Card */}
        <Card className="p-6 border border-rose-200/60 dark:border-rose-900/30 bg-gradient-to-br from-rose-50/30 via-white to-orange-50/20 dark:from-slate-850 dark:to-slate-900 shadow-sm">
          <h3 className="text-sm font-bold uppercase tracking-wider text-rose-600 dark:text-rose-400 mb-4 flex items-center gap-2">
            <Target className="w-4 h-4" /> Focus Areas to Improve
          </h3>

          <div className="space-y-4">
            {weakTopics.slice(0, 4).map((topic) => (
              <div key={topic.topicId} className="space-y-1.5 p-3 rounded-xl bg-white/70 dark:bg-slate-800/60 border border-gray-100 dark:border-slate-700/60 shadow-2xs">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-bold text-gray-900 dark:text-white flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: topic.topicColor || '#f43f5e' }} />
                    {topic.topicName}
                  </span>
                  <div className="flex items-center gap-2">
                    {topic.averageRating && (
                      <span className="text-[11px] font-semibold text-amber-500">
                        ★ {topic.averageRating}/5
                      </span>
                    )}
                    <span className="font-black text-gray-700 dark:text-gray-300">
                      {topic.solvedProblems}/{topic.totalProblems} ({topic.completionPercentage}%)
                    </span>
                  </div>
                </div>

                {/* Progress bar */}
                <div className="w-full bg-gray-100 dark:bg-slate-700 h-1.5 rounded-full overflow-hidden">
                  <div
                    className="h-full rounded-full transition-all duration-500"
                    style={{
                      width: `${topic.completionPercentage}%`,
                      backgroundColor: topic.topicColor || '#f43f5e',
                    }}
                  />
                </div>

                <p className="text-[11px] text-gray-500 dark:text-gray-400">
                  {topic.recommendation}
                </p>
              </div>
            ))}
          </div>
        </Card>

        {/* Adaptive Practice Suggestions */}
        <Card className="p-6 border border-blue-200/60 dark:border-blue-900/30 bg-gradient-to-br from-blue-50/30 via-white to-indigo-50/20 dark:from-slate-850 dark:to-slate-900 shadow-sm">
          <h3 className="text-sm font-bold uppercase tracking-wider text-blue-600 dark:text-blue-400 mb-4 flex items-center gap-2">
            <Sparkles className="w-4 h-4" /> Recommended Practice Problems
          </h3>

          <div className="space-y-3">
            {suggestions.slice(0, 4).map((sug) => (
              <a
                key={sug.taskId}
                href={toSafeUrl(sug.platformLink)}
                target="_blank"
                rel="noopener noreferrer"
                className="group p-3 rounded-xl bg-white/70 dark:bg-slate-800/60 border border-gray-100 dark:border-slate-700/60 shadow-2xs hover:border-blue-400 dark:hover:border-blue-500 flex items-center justify-between gap-3 transition-all block"
              >
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-bold text-xs text-gray-900 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors truncate">
                      {sug.title}
                    </span>
                    <Badge variant={sug.difficulty.toLowerCase() as any}>{sug.difficulty}</Badge>
                  </div>
                  <p className="text-[11px] text-gray-500 dark:text-gray-400 truncate">
                    {sug.reason}
                  </p>
                </div>
                <div className="text-blue-600 dark:text-blue-400 group-hover:translate-x-1 transition-transform flex-shrink-0">
                  <ChevronRight className="w-4 h-4" />
                </div>
              </a>
            ))}
          </div>
        </Card>
      </div>

      {/* Algorithmic Pattern Breakdown */}
      {patterns.length > 0 && (
        <Card className="p-6 border border-gray-200/80 dark:border-slate-800 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-bold uppercase tracking-wider text-gray-800 dark:text-gray-200 flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-emerald-500" /> Algorithmic Pattern Recognition
            </h3>
            <span className="text-xs text-gray-500 dark:text-gray-400">
              Pattern Mastery Breakdown
            </span>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
            {patterns.slice(0, 6).map((pat) => (
              <div
                key={pat.pattern}
                className="p-3 rounded-xl bg-gray-50/70 dark:bg-slate-800/50 border border-gray-200/60 dark:border-slate-700/60 text-center"
              >
                <p className="text-xs font-bold text-gray-900 dark:text-white truncate" title={pat.pattern}>
                  {pat.pattern}
                </p>
                <p className="text-lg font-black text-blue-600 dark:text-blue-400 my-1">
                  {pat.masteryPercentage}%
                </p>
                <p className="text-[10px] text-gray-500 dark:text-gray-400">
                  {pat.solvedCount}/{pat.totalCount} Solved
                </p>
              </div>
            ))}
          </div>
        </Card>
      )}
    </motion.div>
  );
};

export default WeakAreaWidget;
