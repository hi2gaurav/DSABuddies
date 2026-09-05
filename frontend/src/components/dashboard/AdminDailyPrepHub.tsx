import React, { useEffect, useState } from 'react';
import { api } from '../../lib/api';
import { DailyContent, InterviewQuestion } from '../../types';
import Card from '../ui/Card';
import Badge from '../ui/Badge';
import LoadingSpinner from '../ui/LoadingSpinner';
import {
  Crown,
  Sparkles,
  ExternalLink,
  Code2,
  Cpu,
  Layers,
  Database,
  Coffee,
  Leaf,
  Network,
  ChevronDown,
  ChevronUp,
  Copy,
  Check,
  BookOpen,
  Lightbulb,
  Clock,
  HardDrive,
  RefreshCw,
  Building2,
  FileCode
} from 'lucide-react';

export const AdminDailyPrepHub: React.FC = () => {
  const [content, setContent] = useState<DailyContent | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'leetcode' | 'lld' | 'hld' | 'java' | 'spring' | 'database' | 'cs'>('leetcode');
  const [expandedQuestions, setExpandedQuestions] = useState<Record<number, boolean>>({});
  const [questionTab, setQuestionTab] = useState<Record<number, 'summary' | 'solution'>>({});
  const [leetCodeView, setLeetCodeView] = useState<'overview' | 'solution'>('overview');
  const [lldView, setLldView] = useState<'overview' | 'solution'>('overview');
  const [hldView, setHldView] = useState<'overview' | 'solution'>('overview');
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const fetchDailyContent = async () => {
    try {
      setLoading(true);
      const data = await api.getDailyContent();
      setContent(data);
    } catch (err: any) {
      console.error('Failed to load admin daily content', err);
      setError('Failed to load daily prep content.');
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = async () => {
    try {
      setRefreshing(true);
      const data = await api.refreshDailyContent();
      setContent(data);
    } catch (err: any) {
      console.error('Failed to refresh admin daily content', err);
    } finally {
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchDailyContent();
  }, []);

  const toggleQuestion = (id: number) => {
    setExpandedQuestions((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const expandAll = (questions: InterviewQuestion[]) => {
    const all: Record<number, boolean> = {};
    questions.forEach((q) => { all[q.id] = true; });
    setExpandedQuestions(all);
  };

  const collapseAll = () => {
    setExpandedQuestions({});
  };

  const copyToClipboard = (text: string, identifier: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(identifier);
    setTimeout(() => setCopiedId(null), 2000);
  };

  if (loading) {
    return (
      <Card className="p-8 text-center bg-gradient-to-r from-amber-500/5 via-indigo-500/5 to-purple-500/5 border-amber-500/20">
        <LoadingSpinner size="md" />
        <p className="text-xs text-gray-500 dark:text-gray-400 mt-2 font-medium">Curating today's admin prep content...</p>
      </Card>
    );
  }

  if (error || !content) {
    return null; // Gracefully hide if error
  }

  const tabs = [
    { id: 'leetcode', label: 'LeetCode', icon: Code2, count: 1, color: 'text-amber-500' },
    { id: 'lld', label: 'LLD Design', icon: Layers, count: 1, color: 'text-blue-500' },
    { id: 'hld', label: 'HLD System', icon: Cpu, count: 1, color: 'text-purple-500' },
    { id: 'java', label: 'Java Core', icon: Coffee, count: 10, color: 'text-orange-500' },
    { id: 'spring', label: 'Spring Boot', icon: Leaf, count: 10, color: 'text-emerald-500' },
    { id: 'database', label: 'Database & SQL', icon: Database, count: 10, color: 'text-cyan-500' },
    { id: 'cs', label: 'OS & Networks', icon: Network, count: 10, color: 'text-rose-500' },
  ] as const;

  const renderQuestionList = (questions: InterviewQuestion[], categoryTitle: string) => {
    const allExpanded = questions.every((q) => !!expandedQuestions[q.id]);

    return (
      <div className="space-y-4">
        {/* Controls */}
        <div className="flex items-center justify-between border-b border-gray-100 dark:border-slate-700/60 pb-3">
          <div className="flex items-center gap-2">
            <span className="text-sm font-semibold text-gray-900 dark:text-white">
              {categoryTitle}
            </span>
            <span className="text-xs px-2 py-0.5 rounded-full bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400 font-bold">
              10 Questions
            </span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => (allExpanded ? collapseAll() : expandAll(questions))}
              className="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline"
            >
              {allExpanded ? 'Collapse All' : 'Expand All'}
            </button>
          </div>
        </div>

        {/* Questions list */}
        <div className="space-y-3">
          {questions.map((q, idx) => {
            const isExpanded = !!expandedQuestions[q.id];
            const copyKey = `q-${q.id}-${activeTab}`;

            return (
              <div
                key={q.id}
                className={`rounded-xl border transition-all ${
                  isExpanded
                    ? 'border-indigo-200 dark:border-indigo-800/50 bg-white dark:bg-slate-800/90 shadow-sm'
                    : 'border-gray-200 dark:border-slate-700/60 bg-white/60 dark:bg-slate-800/50 hover:border-gray-300 dark:hover:border-slate-600'
                }`}
              >
                {/* Header */}
                <div
                  onClick={() => toggleQuestion(q.id)}
                  className="p-4 flex items-start justify-between gap-3 cursor-pointer select-none"
                >
                  <div className="flex items-start gap-3">
                    <span className="w-6 h-6 rounded-lg bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400 text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">
                      {idx + 1}
                    </span>
                    <div>
                      <div className="flex items-center gap-2 mb-1 flex-wrap">
                        <span className="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-md bg-gray-100 dark:bg-slate-700 text-gray-600 dark:text-gray-300">
                          {q.topic}
                        </span>
                      </div>
                      <h4 className="text-sm font-semibold text-gray-900 dark:text-white leading-snug">
                        {q.question}
                      </h4>
                    </div>
                  </div>

                  <div className="flex items-center gap-1.5 flex-shrink-0 text-gray-400">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        copyToClipboard(`${q.question}\n\n${q.answer}`, copyKey);
                      }}
                      className="p-1 hover:text-gray-600 dark:hover:text-gray-200 rounded-md hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
                      title="Copy Q&A"
                    >
                      {copiedId === copyKey ? (
                        <Check className="w-4 h-4 text-emerald-500" />
                      ) : (
                        <Copy className="w-4 h-4" />
                      )}
                    </button>
                    {isExpanded ? (
                      <ChevronUp className="w-5 h-5 text-gray-400" />
                    ) : (
                      <ChevronDown className="w-5 h-5 text-gray-400" />
                    )}
                  </div>
                </div>

                {/* Expanded Answer */}
                {isExpanded && (
                  <div className="px-4 pb-4 pt-1 border-t border-gray-100 dark:border-slate-700/50 space-y-3">
                    {/* Sub-Tabs: Quick Summary vs Detailed Solution */}
                    <div className="flex items-center gap-2 border-b border-gray-100 dark:border-slate-700/60 pb-2">
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setQuestionTab((prev) => ({ ...prev, [q.id]: 'summary' }));
                        }}
                        className={`px-3 py-1 rounded-lg text-xs font-bold transition-all ${
                          (questionTab[q.id] || 'summary') === 'summary'
                            ? 'bg-indigo-600 text-white shadow-xs'
                            : 'bg-gray-100 dark:bg-slate-700/80 text-gray-600 dark:text-gray-300 hover:bg-gray-200'
                        }`}
                      >
                        Quick Summary
                      </button>
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setQuestionTab((prev) => ({ ...prev, [q.id]: 'solution' }));
                        }}
                        className={`px-3 py-1 rounded-lg text-xs font-bold transition-all flex items-center gap-1.5 ${
                          questionTab[q.id] === 'solution'
                            ? 'bg-amber-500 text-slate-950 shadow-xs'
                            : 'bg-gray-100 dark:bg-slate-700/80 text-gray-600 dark:text-gray-300 hover:bg-gray-200'
                        }`}
                      >
                        <Sparkles className="w-3 h-3" />
                        <span>Detailed Solution & Walkthrough</span>
                      </button>
                    </div>

                    {(questionTab[q.id] || 'summary') === 'summary' ? (
                      <>
                        <div className="text-xs sm:text-sm text-gray-700 dark:text-gray-300 whitespace-pre-line leading-relaxed">
                          {q.answer}
                        </div>

                        {/* Key points */}
                        {q.keyPoints && q.keyPoints.length > 0 && (
                          <div className="p-3 rounded-lg bg-amber-50/50 dark:bg-amber-950/20 border border-amber-200/50 dark:border-amber-800/30">
                            <div className="text-xs font-bold text-amber-800 dark:text-amber-300 flex items-center gap-1.5 mb-1.5">
                              <Lightbulb className="w-3.5 h-3.5" />
                              <span>Key Takeaways for Interviews:</span>
                            </div>
                            <ul className="list-disc list-inside space-y-1 text-xs text-amber-900 dark:text-amber-200/80">
                              {q.keyPoints.map((kp, kIdx) => (
                                <li key={kIdx}>{kp}</li>
                              ))}
                            </ul>
                          </div>
                        )}

                        {/* Code snippet */}
                        {q.codeSnippet && (
                          <div className="relative rounded-lg overflow-hidden border border-slate-700 bg-slate-900 p-3">
                            <pre className="text-xs text-emerald-400 font-mono overflow-x-auto whitespace-pre">
                              {q.codeSnippet}
                            </pre>
                          </div>
                        )}
                      </>
                    ) : (
                      <div className="p-4 rounded-xl bg-slate-900 text-slate-100 border border-slate-700/80 space-y-3 font-sans">
                        <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                          <span className="text-xs font-bold text-amber-400 flex items-center gap-1.5">
                            <Sparkles className="w-3.5 h-3.5" /> Comprehensive Interview Solution
                          </span>
                          <button
                            type="button"
                            onClick={() => copyToClipboard(q.detailedSolution || q.answer, `detailed-${q.id}`)}
                            className="text-xs text-gray-400 hover:text-white flex items-center gap-1"
                          >
                            {copiedId === `detailed-${q.id}` ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                            <span>Copy Solution</span>
                          </button>
                        </div>
                        <div className="text-xs sm:text-sm text-slate-200 whitespace-pre-wrap leading-relaxed">
                          {q.detailedSolution || q.answer}
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  return (
    <Card className="p-0 overflow-hidden border-2 border-amber-400/40 dark:border-amber-500/30 shadow-xl bg-gradient-to-b from-amber-500/[0.03] via-white to-white dark:via-slate-800 dark:to-slate-850 relative">
      {/* Top Banner */}
      <div className="p-5 sm:p-6 bg-gradient-to-r from-amber-500/10 via-indigo-500/10 to-purple-500/10 border-b border-amber-200/50 dark:border-amber-800/30 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1.5">
            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-extrabold bg-amber-500 text-slate-950 shadow-xs">
              <Crown className="w-3.5 h-3.5" />
              <span>ADMIN DAILY PREP HUB</span>
            </span>
            <span className="text-xs font-semibold text-gray-500 dark:text-gray-400 flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" />
              {content.date} (Day #{content.dayOfYear})
            </span>
          </div>
          <h2 className="text-xl sm:text-2xl font-black text-gray-900 dark:text-white flex items-center gap-2">
            <span>Daily Masterclass & Interview Prep</span>
            <Sparkles className="w-5 h-5 text-amber-500" />
          </h2>
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-1 italic">
            "{content.quoteOfTheDay}"
          </p>
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          <button
            onClick={handleRefresh}
            disabled={refreshing}
            className="text-xs px-3.5 py-2 rounded-xl bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold shadow-xs transition-all flex items-center gap-1.5 disabled:opacity-50 btn-press"
            title="Refresh questions to align with latest interview trends"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? 'animate-spin' : ''}`} />
            <span>{refreshing ? 'Refreshing...' : 'Refresh Today\'s Questions'}</span>
          </button>
          <span className="text-xs px-3 py-1.5 rounded-xl bg-white dark:bg-slate-700/80 border border-gray-200 dark:border-slate-600 text-gray-700 dark:text-gray-200 font-semibold shadow-xs">
            ✨ Auto-refreshed Daily at 12:00 AM
          </span>
        </div>
      </div>

      {/* Tabs Row */}
      <div className="px-4 pt-3 border-b border-gray-200 dark:border-slate-700 bg-gray-50/50 dark:bg-slate-800/50 overflow-x-auto flex items-center gap-1 scrollbar-none">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;

          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`flex items-center gap-2 px-3.5 py-2.5 text-xs font-bold rounded-t-xl transition-all whitespace-nowrap border-b-2 ${
                isActive
                  ? 'border-amber-500 text-amber-600 dark:text-amber-400 bg-white dark:bg-slate-800 shadow-xs'
                  : 'border-transparent text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white hover:bg-white/50 dark:hover:bg-slate-700/40'
              }`}
            >
              <Icon className={`w-4 h-4 ${tab.color}`} />
              <span>{tab.label}</span>
              <span
                className={`text-[10px] px-1.5 py-0.2 rounded-full font-bold ${
                  isActive
                    ? 'bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300'
                    : 'bg-gray-200 dark:bg-slate-700 text-gray-600 dark:text-gray-400'
                }`}
              >
                {tab.count}
              </span>
            </button>
          );
        })}
      </div>

      {/* Tab Content Area */}
      <div className="p-5 sm:p-6">
        {/* TAB 1: LEETCODE */}
        {activeTab === 'leetcode' && (
          <div className="space-y-4">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-gradient-to-r from-amber-500/10 via-orange-500/5 to-transparent p-4 rounded-xl border border-amber-500/20">
              <div>
                <div className="flex items-center gap-2 mb-1 flex-wrap">
                  <Badge variant={content.leetCodeProblem.difficulty.toLowerCase() as any}>
                    {content.leetCodeProblem.difficulty}
                  </Badge>
                  <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">
                    {content.leetCodeProblem.topic}
                  </span>
                  {content.leetCodeProblem.companies && content.leetCodeProblem.companies.length > 0 && (
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400 flex items-center gap-1">
                      <Building2 className="w-3 h-3" />
                      Asked by: {content.leetCodeProblem.companies.join(', ')}
                    </span>
                  )}
                </div>
                <h3 className="text-lg font-bold text-gray-900 dark:text-white">
                  {content.leetCodeProblem.title}
                </h3>
              </div>

              <div className="flex items-center gap-2 flex-wrap">
                <a
                  href={content.leetCodeProblem.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1.5 px-4 py-2 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold rounded-xl text-xs transition-all shadow-sm shadow-amber-500/20 flex-shrink-0"
                >
                  <span>Solve on LeetCode</span>
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>
              </div>
            </div>

            {/* View Toggle */}
            <div className="flex items-center gap-2 border-b border-gray-200 dark:border-slate-700 pb-2">
              <button
                onClick={() => setLeetCodeView('overview')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                  leetCodeView === 'overview'
                    ? 'bg-amber-500 text-slate-950 shadow-xs'
                    : 'bg-gray-100 dark:bg-slate-700/80 text-gray-600 dark:text-gray-300 hover:bg-gray-200'
                }`}
              >
                Problem Overview
              </button>
              <button
                onClick={() => setLeetCodeView('solution')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all flex items-center gap-1.5 ${
                  leetCodeView === 'solution'
                    ? 'bg-indigo-600 text-white shadow-xs'
                    : 'bg-gray-100 dark:bg-slate-700/80 text-gray-600 dark:text-gray-300 hover:bg-gray-200'
                }`}
              >
                <Sparkles className="w-3.5 h-3.5 text-amber-300" />
                <span>Detailed Solution & Code Walkthrough</span>
              </button>
            </div>

            {leetCodeView === 'overview' ? (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="p-4 rounded-xl bg-gray-50 dark:bg-slate-800/60 border border-gray-200 dark:border-slate-700/60">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-2 flex items-center gap-1.5">
                    <BookOpen className="w-3.5 h-3.5 text-blue-500" />
                    <span>Problem Statement</span>
                  </h4>
                  <p className="text-sm text-gray-700 dark:text-gray-300 leading-relaxed">
                    {content.leetCodeProblem.problemSummary}
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-gray-50 dark:bg-slate-800/60 border border-gray-200 dark:border-slate-700/60">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-2 flex items-center gap-1.5">
                    <Lightbulb className="w-3.5 h-3.5 text-amber-500" />
                    <span>Optimal Approach & Strategy</span>
                  </h4>
                  <p className="text-sm text-gray-700 dark:text-gray-300 leading-relaxed">
                    {content.leetCodeProblem.optimalApproach}
                  </p>

                  <div className="mt-4 pt-3 border-t border-gray-200 dark:border-slate-700/60 flex items-center gap-4 flex-wrap text-xs">
                    <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                      ⏱ Time: {content.leetCodeProblem.timeComplexity}
                    </span>
                    <span className="font-semibold text-indigo-600 dark:text-indigo-400">
                      💾 Space: {content.leetCodeProblem.spaceComplexity}
                    </span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="p-5 rounded-2xl bg-slate-900 border border-slate-700/80 text-slate-100 space-y-4">
                <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                  <div className="flex items-center gap-2">
                    <FileCode className="w-4 h-4 text-amber-400" />
                    <span className="text-sm font-bold text-white">Full Optimal Solution & Complexity Analysis</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => copyToClipboard(content.leetCodeProblem.detailedSolution || content.leetCodeProblem.optimalApproach, 'lc-solution')}
                    className="text-xs text-gray-400 hover:text-white flex items-center gap-1.5 px-3 py-1 bg-slate-800 rounded-lg border border-slate-700"
                  >
                    {copiedId === 'lc-solution' ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copiedId === 'lc-solution' ? 'Copied!' : 'Copy Code & Solution'}</span>
                  </button>
                </div>
                <div className="text-xs sm:text-sm text-slate-200 whitespace-pre-wrap leading-relaxed font-mono">
                  {content.leetCodeProblem.detailedSolution || content.leetCodeProblem.optimalApproach}
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 2: LLD TOPIC */}
        {activeTab === 'lld' && (
          <div className="space-y-4">
            <div className="bg-gradient-to-r from-blue-500/10 to-transparent p-4 rounded-xl border border-blue-500/20">
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-extrabold px-2.5 py-0.5 rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300">
                  {content.lldTopic.id} • {content.lldTopic.difficulty}
                </span>
              </div>
              <h3 className="text-xl font-bold text-gray-900 dark:text-white">
                {content.lldTopic.title}
              </h3>
              <p className="text-sm text-gray-600 dark:text-gray-300 mt-1">
                {content.lldTopic.description}
              </p>
            </div>

            {/* LLD View Toggle */}
            <div className="flex items-center gap-2 border-b border-gray-200 dark:border-slate-700 pb-2">
              <button
                onClick={() => setLldView('overview')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                  lldView === 'overview'
                    ? 'bg-blue-600 text-white shadow-xs'
                    : 'bg-gray-100 dark:bg-slate-700/80 text-gray-600 dark:text-gray-300 hover:bg-gray-200'
                }`}
              >
                Requirements & Patterns
              </button>
              <button
                onClick={() => setLldView('solution')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all flex items-center gap-1.5 ${
                  lldView === 'solution'
                    ? 'bg-amber-500 text-slate-950 shadow-xs'
                    : 'bg-gray-100 dark:bg-slate-700/80 text-gray-600 dark:text-gray-300 hover:bg-gray-200'
                }`}
              >
                <Sparkles className="w-3.5 h-3.5" />
                <span>Detailed Architecture Walkthrough</span>
              </button>
            </div>

            {lldView === 'overview' ? (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="p-4 rounded-xl bg-gray-50 dark:bg-slate-800/60 border border-gray-200 dark:border-slate-700/60">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-3 flex items-center gap-1.5">
                    <Layers className="w-3.5 h-3.5 text-blue-500" />
                    <span>Core Functional Requirements</span>
                  </h4>
                  <ul className="space-y-2 text-xs sm:text-sm text-gray-700 dark:text-gray-300 list-disc list-inside">
                    {content.lldTopic.coreRequirements.map((req, idx) => (
                      <li key={idx} className="leading-relaxed">{req}</li>
                    ))}
                  </ul>
                </div>

                <div className="p-4 rounded-xl bg-gray-50 dark:bg-slate-800/60 border border-gray-200 dark:border-slate-700/60">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-3 flex items-center gap-1.5">
                    <Code2 className="w-3.5 h-3.5 text-indigo-500" />
                    <span>Design Patterns Applied</span>
                  </h4>
                  <ul className="space-y-2 text-xs sm:text-sm text-gray-700 dark:text-gray-300 list-disc list-inside">
                    {content.lldTopic.designPatternsOrComponents.map((pat, idx) => (
                      <li key={idx} className="leading-relaxed">{pat}</li>
                    ))}
                  </ul>

                  <div className="mt-4 pt-3 border-t border-gray-200 dark:border-slate-700/60">
                    <span className="text-xs font-bold text-gray-500 dark:text-gray-400 block mb-1">
                      Class Hierarchy Summary:
                    </span>
                    <p className="text-xs text-indigo-700 dark:text-indigo-300 bg-indigo-50 dark:bg-indigo-950/40 p-2.5 rounded-lg font-mono">
                      {content.lldTopic.architectureSummary}
                    </p>
                  </div>
                </div>
              </div>
            ) : (
              <div className="p-5 rounded-2xl bg-slate-900 border border-slate-700/80 text-slate-100 space-y-4">
                <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                  <div className="flex items-center gap-2">
                    <Layers className="w-4 h-4 text-blue-400" />
                    <span className="text-sm font-bold text-white">Full LLD Class Design & Trade-offs</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => copyToClipboard(content.lldTopic.detailedSolution || content.lldTopic.architectureSummary, 'lld-solution')}
                    className="text-xs text-gray-400 hover:text-white flex items-center gap-1.5 px-3 py-1 bg-slate-800 rounded-lg border border-slate-700"
                  >
                    {copiedId === 'lld-solution' ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copiedId === 'lld-solution' ? 'Copied!' : 'Copy Architecture'}</span>
                  </button>
                </div>
                <div className="text-xs sm:text-sm text-slate-200 whitespace-pre-wrap leading-relaxed font-sans">
                  {content.lldTopic.detailedSolution || content.lldTopic.architectureSummary}
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 3: HLD TOPIC */}
        {activeTab === 'hld' && (
          <div className="space-y-4">
            <div className="bg-gradient-to-r from-purple-500/10 to-transparent p-4 rounded-xl border border-purple-500/20">
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-extrabold px-2.5 py-0.5 rounded-full bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-300">
                  {content.hldTopic.id} • {content.hldTopic.difficulty}
                </span>
              </div>
              <h3 className="text-xl font-bold text-gray-900 dark:text-white">
                {content.hldTopic.title}
              </h3>
              <p className="text-sm text-gray-600 dark:text-gray-300 mt-1">
                {content.hldTopic.description}
              </p>
            </div>

            {/* HLD View Toggle */}
            <div className="flex items-center gap-2 border-b border-gray-200 dark:border-slate-700 pb-2">
              <button
                onClick={() => setHldView('overview')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                  hldView === 'overview'
                    ? 'bg-purple-600 text-white shadow-xs'
                    : 'bg-gray-100 dark:bg-slate-700/80 text-gray-600 dark:text-gray-300 hover:bg-gray-200'
                }`}
              >
                High-Scale Overview
              </button>
              <button
                onClick={() => setHldView('solution')}
                className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all flex items-center gap-1.5 ${
                  hldView === 'solution'
                    ? 'bg-amber-500 text-slate-950 shadow-xs'
                    : 'bg-gray-100 dark:bg-slate-700/80 text-gray-600 dark:text-gray-300 hover:bg-gray-200'
                }`}
              >
                <Sparkles className="w-3.5 h-3.5" />
                <span>Detailed System Architecture</span>
              </button>
            </div>

            {hldView === 'overview' ? (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="p-4 rounded-xl bg-gray-50 dark:bg-slate-800/60 border border-gray-200 dark:border-slate-700/60">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-3 flex items-center gap-1.5">
                    <Cpu className="w-3.5 h-3.5 text-purple-500" />
                    <span>High-Scale System Requirements</span>
                  </h4>
                  <ul className="space-y-2 text-xs sm:text-sm text-gray-700 dark:text-gray-300 list-disc list-inside">
                    {content.hldTopic.coreRequirements.map((req, idx) => (
                      <li key={idx} className="leading-relaxed">{req}</li>
                    ))}
                  </ul>
                </div>

                <div className="p-4 rounded-xl bg-gray-50 dark:bg-slate-800/60 border border-gray-200 dark:border-slate-700/60">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-3 flex items-center gap-1.5">
                    <HardDrive className="w-3.5 h-3.5 text-indigo-500" />
                    <span>Architecture Building Blocks</span>
                  </h4>
                  <ul className="space-y-2 text-xs sm:text-sm text-gray-700 dark:text-gray-300 list-disc list-inside">
                    {content.hldTopic.designPatternsOrComponents.map((pat, idx) => (
                      <li key={idx} className="leading-relaxed">{pat}</li>
                    ))}
                  </ul>

                  <div className="mt-4 pt-3 border-t border-gray-200 dark:border-slate-700/60">
                    <span className="text-xs font-bold text-gray-500 dark:text-gray-400 block mb-1">
                      End-to-End Data Pipeline:
                    </span>
                    <p className="text-xs text-purple-700 dark:text-purple-300 bg-purple-50 dark:bg-purple-950/40 p-2.5 rounded-lg font-mono">
                      {content.hldTopic.architectureSummary}
                    </p>
                  </div>
                </div>
              </div>
            ) : (
              <div className="p-5 rounded-2xl bg-slate-900 border border-slate-700/80 text-slate-100 space-y-4">
                <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                  <div className="flex items-center gap-2">
                    <Cpu className="w-4 h-4 text-purple-400" />
                    <span className="text-sm font-bold text-white">Full System Architecture & Scaling Blueprint</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => copyToClipboard(content.hldTopic.detailedSolution || content.hldTopic.architectureSummary, 'hld-solution')}
                    className="text-xs text-gray-400 hover:text-white flex items-center gap-1.5 px-3 py-1 bg-slate-800 rounded-lg border border-slate-700"
                  >
                    {copiedId === 'hld-solution' ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copiedId === 'hld-solution' ? 'Copied!' : 'Copy Architecture'}</span>
                  </button>
                </div>
                <div className="text-xs sm:text-sm text-slate-200 whitespace-pre-wrap leading-relaxed font-sans">
                  {content.hldTopic.detailedSolution || content.hldTopic.architectureSummary}
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 4: JAVA CORE */}
        {activeTab === 'java' && renderQuestionList(content.javaQuestions, 'Java Core & Concurrency Interview Questions')}

        {/* TAB 5: SPRING BOOT */}
        {activeTab === 'spring' && renderQuestionList(content.springBootQuestions, 'Spring Boot & Microservices Interview Questions')}

        {/* TAB 6: DATABASE */}
        {activeTab === 'database' && renderQuestionList(content.databaseQuestions, 'Database, SQL & System Scaling Interview Questions')}

        {/* TAB 7: CS SUBJECTS */}
        {activeTab === 'cs' && renderQuestionList(content.csSubjectsQuestions, 'OS & Computer Networks Interview Questions')}
      </div>
    </Card>
  );
};

export default AdminDailyPrepHub;
