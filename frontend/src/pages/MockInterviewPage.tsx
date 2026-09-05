import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import {
  Timer, Award, CheckCircle2, ArrowRight, ArrowLeft,
  Play, RotateCcw, ExternalLink, Star, FileText, Check, Sparkles, Shield
} from 'lucide-react';
import { api } from '../lib/api';
import { MockSession, MockQuestion } from '../types';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import CoinRewardOverlay from '../components/common/CoinRewardOverlay';
import { useToast } from '../components/ui/Toast';
import { triggerCelebrationConfetti } from '../lib/confetti';
import { toSafeUrl } from '../lib/security';

export const MockInterviewPage: React.FC = () => {
  const [activeSession, setActiveSession] = useState<MockSession | null>(null);
  const [pastSessions, setPastSessions] = useState<MockSession[]>([]);
  const [activeTab, setActiveTab] = useState<'setup' | 'interview' | 'results' | 'history'>('setup');
  const [loading, setLoading] = useState(false);

  // Setup form states
  const [mode, setMode] = useState<'DSA' | 'SYSTEM_DESIGN' | 'BEHAVIORAL'>('DSA');
  const [difficulty, setDifficulty] = useState<string>('MIXED');
  const [timeLimit, setTimeLimit] = useState<number>(45);
  const [questionCount, setQuestionCount] = useState<number>(2);

  // Active interview states
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState<number>(0);
  const [timeLeftSeconds, setTimeLeftSeconds] = useState<number>(45 * 60);
  const [userNotes, setUserNotes] = useState<string>('');
  const [selfRating, setSelfRating] = useState<number>(4);
  const [rewardXp, setRewardXp] = useState<number | null>(null);

  const { show } = useToast();

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      const data = await api.getMockHistory();
      setPastSessions(data);
    } catch (_) {}
  };

  // Timer countdown
  useEffect(() => {
    if (activeTab !== 'interview' || !activeSession) return;
    if (timeLeftSeconds <= 0) {
      handleCompleteSession();
      return;
    }

    const timer = setInterval(() => {
      setTimeLeftSeconds((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [activeTab, activeSession, timeLeftSeconds]);

  // Load question notes when index changes
  useEffect(() => {
    if (activeSession && activeSession.questions[currentQuestionIndex]) {
      const q = activeSession.questions[currentQuestionIndex];
      setUserNotes(q.userNotes || '');
      setSelfRating(q.selfRating || 4);
    }
  }, [currentQuestionIndex, activeSession]);

  const handleStartInterview = async () => {
    setLoading(true);
    try {
      const session = await api.startMockSession({
        mode,
        difficultyFilter: difficulty,
        timeLimitMinutes: timeLimit,
        questionCount,
      });
      setActiveSession(session);
      setTimeLeftSeconds(timeLimit * 60);
      setCurrentQuestionIndex(0);
      setActiveTab('interview');
      show(`Mock ${mode} Interview Started! ⏰ Good luck!`, 'info');
    } catch (err) {
      show('Failed to start mock session', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveCurrentAnswer = async (markAnswered = true) => {
    if (!activeSession) return;
    const currentQ = activeSession.questions[currentQuestionIndex];
    if (!currentQ) return;

    try {
      const updated = await api.submitMockAnswer(activeSession.id, currentQ.id, {
        userNotes,
        selfRating,
        answered: markAnswered,
      });
      setActiveSession(updated);
      show('Answer & notes recorded', 'success');
    } catch (err) {
      show('Failed to save answer', 'error');
    }
  };

  const handleCompleteSession = async () => {
    if (!activeSession) return;
    setLoading(true);
    try {
      await handleSaveCurrentAnswer(true);
      const completed = await api.completeMockSession(activeSession.id);
      setActiveSession(completed);
      setRewardXp(completed.xpAwarded);
      setActiveTab('results');
      triggerCelebrationConfetti();
      fetchHistory();
      show(`Interview Finished! +${completed.xpAwarded} XP Earned! 🏆`, 'success');
    } catch (err) {
      show('Failed to complete session', 'error');
    } finally {
      setLoading(false);
    }
  };

  const formatTimer = (secs: number) => {
    const mins = Math.floor(secs / 60);
    const s = secs % 60;
    return `${mins.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const currentQ: MockQuestion | undefined = activeSession?.questions[currentQuestionIndex];

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Coin Reward Overlay */}
      {rewardXp !== null && (
        <CoinRewardOverlay
          xp={rewardXp}
          onComplete={() => setRewardXp(null)}
        />
      )}

      {/* Top Mode Header */}
      {activeTab !== 'interview' && (
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-black text-gray-900 dark:text-white flex items-center gap-2.5">
              <span>Mock Interview Simulator</span>
              <Timer className="w-8 h-8 text-blue-500" />
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
              Time-pressured technical assessments designed to replicate FAANG/Tier-1 interview settings.
            </p>
          </div>

          <div className="flex items-center gap-2 bg-gray-100 dark:bg-slate-800 p-1 rounded-xl text-xs font-bold">
            <button
              onClick={() => setActiveTab('setup')}
              className={`px-3.5 py-1.5 rounded-lg transition-all ${
                activeTab === 'setup'
                  ? 'bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-xs'
                  : 'text-gray-500 dark:text-gray-400 hover:text-gray-700'
              }`}
            >
              New Interview
            </button>
            <button
              onClick={() => setActiveTab('history')}
              className={`px-3.5 py-1.5 rounded-lg transition-all ${
                activeTab === 'history'
                  ? 'bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-xs'
                  : 'text-gray-500 dark:text-gray-400 hover:text-gray-700'
              }`}
            >
              History ({pastSessions.length})
            </button>
          </div>
        </div>
      )}

      {/* TAB 1: SETUP SCREEN */}
      {activeTab === 'setup' && (
        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Mode 1: DSA */}
            <div
              onClick={() => setMode('DSA')}
              className={`cursor-pointer p-6 rounded-3xl border-2 transition-all text-center space-y-3 ${
                mode === 'DSA'
                  ? 'border-blue-500 bg-blue-50/50 dark:bg-blue-950/20 shadow-lg shadow-blue-500/10'
                  : 'border-gray-200 dark:border-slate-800 hover:border-gray-300 dark:hover:border-slate-700 bg-white dark:bg-slate-900'
              }`}
            >
              <div className="w-14 h-14 rounded-2xl bg-blue-500 text-white flex items-center justify-center mx-auto shadow-md shadow-blue-500/30">
                <Star className="w-7 h-7 fill-current" />
              </div>
              <div>
                <h3 className="text-base font-black text-gray-900 dark:text-white">Data Structures & Algorithms</h3>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  Randomized coding problems with algorithmic constraints and edge cases.
                </p>
              </div>
            </div>

            {/* Mode 2: System Design */}
            <div
              onClick={() => setMode('SYSTEM_DESIGN')}
              className={`cursor-pointer p-6 rounded-3xl border-2 transition-all text-center space-y-3 ${
                mode === 'SYSTEM_DESIGN'
                  ? 'border-purple-500 bg-purple-50/50 dark:bg-purple-950/20 shadow-lg shadow-purple-500/10'
                  : 'border-gray-200 dark:border-slate-800 hover:border-gray-300 dark:hover:border-slate-700 bg-white dark:bg-slate-900'
              }`}
            >
              <div className="w-14 h-14 rounded-2xl bg-purple-500 text-white flex items-center justify-center mx-auto shadow-md shadow-purple-500/30">
                <Shield className="w-7 h-7" />
              </div>
              <div>
                <h3 className="text-base font-black text-gray-900 dark:text-white">System Design (HLD/LLD)</h3>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  High-level scalability architecture and low-level OOP class diagrams.
                </p>
              </div>
            </div>

            {/* Mode 3: Behavioral */}
            <div
              onClick={() => setMode('BEHAVIORAL')}
              className={`cursor-pointer p-6 rounded-3xl border-2 transition-all text-center space-y-3 ${
                mode === 'BEHAVIORAL'
                  ? 'border-amber-500 bg-amber-50/50 dark:bg-amber-950/20 shadow-lg shadow-amber-500/10'
                  : 'border-gray-200 dark:border-slate-800 hover:border-gray-300 dark:hover:border-slate-700 bg-white dark:bg-slate-900'
              }`}
            >
              <div className="w-14 h-14 rounded-2xl bg-amber-500 text-white flex items-center justify-center mx-auto shadow-md shadow-amber-500/30">
                <Sparkles className="w-7 h-7" />
              </div>
              <div>
                <h3 className="text-base font-black text-gray-900 dark:text-white">Behavioral (STAR Method)</h3>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  Leadership, incident management, technical conflicts, and project impact.
                </p>
              </div>
            </div>
          </div>

          {/* Configuration Card */}
          <Card className="p-6 space-y-6">
            <h3 className="text-base font-bold text-gray-900 dark:text-white">Interview Configuration</h3>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
              {/* Difficulty */}
              <div className="space-y-2">
                <label className="text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">
                  Difficulty
                </label>
                <div className="grid grid-cols-2 gap-2">
                  {['MIXED', 'EASY', 'MEDIUM', 'HARD'].map((d) => (
                    <button
                      key={d}
                      type="button"
                      onClick={() => setDifficulty(d)}
                      className={`py-2 px-3 rounded-xl text-xs font-bold transition-all border ${
                        difficulty === d
                          ? 'border-blue-500 bg-blue-50 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400'
                          : 'border-gray-200 dark:border-slate-700 text-gray-600 dark:text-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      {d}
                    </button>
                  ))}
                </div>
              </div>

              {/* Time Limit */}
              <div className="space-y-2">
                <label className="text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">
                  Time Limit
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {[30, 45, 60].map((mins) => (
                    <button
                      key={mins}
                      type="button"
                      onClick={() => setTimeLimit(mins)}
                      className={`py-2 px-2 rounded-xl text-xs font-bold transition-all border ${
                        timeLimit === mins
                          ? 'border-blue-500 bg-blue-50 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400'
                          : 'border-gray-200 dark:border-slate-700 text-gray-600 dark:text-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      {mins}m
                    </button>
                  ))}
                </div>
              </div>

              {/* Question Count */}
              <div className="space-y-2">
                <label className="text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider block">
                  Questions
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {[1, 2, 3].map((count) => (
                    <button
                      key={count}
                      type="button"
                      onClick={() => setQuestionCount(count)}
                      className={`py-2 px-2 rounded-xl text-xs font-bold transition-all border ${
                        questionCount === count
                          ? 'border-blue-500 bg-blue-50 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400'
                          : 'border-gray-200 dark:border-slate-700 text-gray-600 dark:text-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      {count} {count === 1 ? 'Problem' : 'Problems'}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-gray-100 dark:border-slate-800 flex justify-end">
              <button
                onClick={handleStartInterview}
                disabled={loading}
                className="px-8 py-3.5 rounded-2xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-extrabold text-sm shadow-lg shadow-blue-500/25 flex items-center gap-2.5 active:scale-98 transition-all disabled:opacity-50"
              >
                <Play className="w-4 h-4 fill-current" />
                <span>Begin Timed Mock Session</span>
              </button>
            </div>
          </Card>
        </motion.div>
      )}

      {/* TAB 2: ACTIVE INTERVIEW FULLSCREEN VIEW */}
      {activeTab === 'interview' && activeSession && currentQ && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-5">
          {/* Top Interview Navigation Bar */}
          <div className="flex items-center justify-between p-4 bg-white dark:bg-slate-900 rounded-2xl border border-gray-200 dark:border-slate-800 shadow-md">
            <div className="flex items-center gap-2">
              <span className="text-xs font-black uppercase tracking-wider px-2.5 py-1 rounded-lg bg-blue-100 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400">
                {activeSession.mode} Interview
              </span>

              {/* Question Index Pills */}
              <div className="flex items-center gap-1.5 ml-2">
                {activeSession.questions.map((q, idx) => (
                  <button
                    key={q.id}
                    onClick={() => {
                      handleSaveCurrentAnswer(false);
                      setCurrentQuestionIndex(idx);
                    }}
                    className={`px-3 py-1 rounded-lg text-xs font-bold transition-all ${
                      currentQuestionIndex === idx
                        ? 'bg-blue-600 text-white shadow-xs'
                        : q.answered
                        ? 'bg-emerald-100 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400'
                        : 'bg-gray-100 dark:bg-slate-800 text-gray-500'
                    }`}
                  >
                    Q{idx + 1} {q.answered && '✓'}
                  </button>
                ))}
              </div>
            </div>

            {/* Countdown Timer */}
            <div className="flex items-center gap-4">
              <div className={`flex items-center gap-2 px-4 py-1.5 rounded-xl font-mono text-base font-black border ${
                timeLeftSeconds < 180
                  ? 'bg-red-50 dark:bg-red-950/40 text-red-600 border-red-300 animate-pulse'
                  : timeLeftSeconds < 600
                  ? 'bg-amber-50 dark:bg-amber-950/40 text-amber-600 border-amber-300'
                  : 'bg-gray-100 dark:bg-slate-800 text-gray-900 dark:text-white border-gray-200 dark:border-slate-700'
              }`}>
                <Timer className="w-4 h-4" />
                <span>{formatTimer(timeLeftSeconds)}</span>
              </div>

              <button
                onClick={handleCompleteSession}
                className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-black shadow-md shadow-emerald-500/20 active:scale-95 transition-all"
              >
                Finish & Submit
              </button>
            </div>
          </div>

          {/* Main Interview Two-Column Layout */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
            {/* Left Column: Problem Statement & Details */}
            <Card className="p-6 space-y-4">
              <div className="flex items-center justify-between gap-2">
                <span className="text-xs font-bold text-gray-400">
                  Question {currentQuestionIndex + 1} of {activeSession.questions.length}
                </span>
                <div className="flex items-center gap-2">
                  <Badge variant={currentQ.difficulty.toLowerCase() as any}>{currentQ.difficulty}</Badge>
                  <Badge>{currentQ.topicName}</Badge>
                </div>
              </div>

              <div>
                <h2 className="text-xl font-black text-gray-900 dark:text-white">
                  {currentQ.title}
                </h2>
                {currentQ.link && (
                  <a
                    href={toSafeUrl(currentQ.link)}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="mt-1.5 inline-flex items-center gap-1.5 text-xs font-bold text-blue-600 dark:text-blue-400 hover:underline"
                  >
                    <span>Open Challenge in New Tab</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </a>
                )}
              </div>

              <div className="p-4 rounded-2xl bg-gray-50 dark:bg-slate-800/60 border border-gray-100 dark:border-slate-800 text-xs text-gray-700 dark:text-gray-300 whitespace-pre-wrap leading-relaxed">
                {currentQ.description || 'Analyze the constraints, identify data structures, and explain your optimal approach before coding.'}
              </div>

              {/* Structured Checklist for Candidate */}
              <div className="p-4 rounded-2xl bg-blue-50/40 dark:bg-blue-950/20 border border-blue-100 dark:border-blue-900/30 space-y-2">
                <h4 className="text-xs font-bold text-blue-700 dark:text-blue-300 flex items-center gap-1.5">
                  <CheckCircle2 className="w-3.5 h-3.5" /> Interview Rubric Checklist
                </h4>
                <ul className="text-[11px] text-gray-600 dark:text-gray-400 space-y-1 list-disc list-inside">
                  <li>Clarify input bounds, null/empty edge cases</li>
                  <li>State Brute-force approach before jumping to optimal</li>
                  <li>State Time & Space Complexity explicitly (e.g. O(N log N) / O(1))</li>
                  <li>Dry run with a test case before submitting</li>
                </ul>
              </div>
            </Card>

            {/* Right Column: Scratchpad & Confidence Rating */}
            <Card className="p-6 space-y-4 flex flex-col justify-between">
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <label className="text-xs font-bold text-gray-700 dark:text-gray-300 flex items-center gap-1.5">
                    <FileText className="w-4 h-4 text-blue-500" /> Candidate Notes & Solution Scratchpad
                  </label>
                  <span className="text-[10px] text-gray-400">Markdown & pseudocode supported</span>
                </div>

                <textarea
                  value={userNotes}
                  onChange={(e) => setUserNotes(e.target.value)}
                  placeholder="Draft your approach, algorithmic steps, time complexity, and pseudocode here..."
                  className="w-full h-64 p-4 rounded-2xl border border-gray-200 dark:border-slate-700 bg-gray-50 dark:bg-slate-900 font-mono text-xs text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-blue-500 resize-none leading-relaxed"
                />

                {/* Self Confidence Rating */}
                <div className="p-3 bg-gray-50 dark:bg-slate-800/60 rounded-xl flex items-center justify-between">
                  <span className="text-xs font-bold text-gray-600 dark:text-gray-400">
                    Self Rating:
                  </span>
                  <div className="flex items-center gap-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <button
                        key={star}
                        type="button"
                        onClick={() => setSelfRating(star)}
                        className="p-1 text-amber-500 hover:scale-110 transition-transform"
                      >
                        <Star
                          className={`w-5 h-5 ${star <= selfRating ? 'fill-current' : 'text-gray-300 dark:text-slate-600'}`}
                        />
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              {/* Bottom Next/Prev Action Buttons */}
              <div className="flex items-center justify-between pt-4 border-t border-gray-100 dark:border-slate-800">
                <button
                  type="button"
                  disabled={currentQuestionIndex === 0}
                  onClick={() => {
                    handleSaveCurrentAnswer(false);
                    setCurrentQuestionIndex((prev) => prev - 1);
                  }}
                  className="px-4 py-2 rounded-xl text-xs font-bold text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-slate-800 disabled:opacity-40 flex items-center gap-1.5"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Previous
                </button>

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => handleSaveCurrentAnswer(true)}
                    className="px-4 py-2 rounded-xl text-xs font-bold bg-blue-100 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400 hover:bg-blue-200 transition-colors"
                  >
                    Save Draft
                  </button>

                  {currentQuestionIndex < activeSession.questions.length - 1 ? (
                    <button
                      type="button"
                      onClick={() => {
                        handleSaveCurrentAnswer(true);
                        setCurrentQuestionIndex((prev) => prev + 1);
                      }}
                      className="px-5 py-2 rounded-xl text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white shadow-md shadow-blue-500/20 flex items-center gap-1.5"
                    >
                      <span>Next Question</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={handleCompleteSession}
                      className="px-5 py-2 rounded-xl text-xs font-bold bg-emerald-600 hover:bg-emerald-700 text-white shadow-md shadow-emerald-500/20 flex items-center gap-1.5"
                    >
                      <Check className="w-3.5 h-3.5 stroke-[3]" />
                      <span>Finish Interview</span>
                    </button>
                  )}
                </div>
              </div>
            </Card>
          </div>
        </motion.div>
      )}

      {/* TAB 3: RESULTS SUMMARY REPORT */}
      {activeTab === 'results' && activeSession && (
        <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="space-y-6">
          <Card className="p-8 text-center space-y-4 bg-gradient-to-b from-blue-500/10 to-indigo-500/5 border border-blue-200 dark:border-blue-900/40">
            <div className="w-18 h-18 rounded-3xl bg-gradient-to-tr from-blue-600 to-indigo-600 text-white flex items-center justify-center mx-auto shadow-xl shadow-blue-500/25">
              <Award className="w-10 h-10" />
            </div>

            <div>
              <span className="text-xs font-black uppercase tracking-wider text-blue-600 dark:text-blue-400">
                Mock Assessment Report
              </span>
              <h2 className="text-2xl sm:text-3xl font-black text-gray-900 dark:text-white mt-1">
                Interview Completed!
              </h2>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1 max-w-md mx-auto">
                Great job completing your timed session. Consistent practice builds calmness and speed under pressure.
              </p>
            </div>

            {/* Score & XP Highlights */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 max-w-md mx-auto pt-2">
              <div className="p-3 bg-white dark:bg-slate-800 rounded-2xl shadow-xs">
                <span className="text-[10px] font-bold text-gray-400 uppercase">Score</span>
                <p className="text-lg font-black text-emerald-500">
                  {activeSession.score} / {activeSession.questions.length}
                </p>
              </div>

              <div className="p-3 bg-white dark:bg-slate-800 rounded-2xl shadow-xs">
                <span className="text-[10px] font-bold text-gray-400 uppercase">XP Awarded</span>
                <p className="text-lg font-black text-amber-500">
                  +{activeSession.xpAwarded} XP
                </p>
              </div>

              <div className="p-3 bg-white dark:bg-slate-800 rounded-2xl shadow-xs col-span-2 sm:col-span-1">
                <span className="text-[10px] font-bold text-gray-400 uppercase">Status</span>
                <p className="text-lg font-black text-blue-500">
                  {activeSession.status}
                </p>
              </div>
            </div>

            <div className="pt-4 flex justify-center gap-3">
              <button
                onClick={() => setActiveTab('setup')}
                className="px-6 py-2.5 rounded-xl text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white shadow-md shadow-blue-500/20 active:scale-95 transition-all"
              >
                Start Another Session
              </button>
              <button
                onClick={() => setActiveTab('history')}
                className="px-6 py-2.5 rounded-xl text-xs font-bold bg-gray-100 dark:bg-slate-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 transition-all"
              >
                View History
              </button>
            </div>
          </Card>

          {/* Breakdown of each question */}
          <div className="space-y-3">
            <h3 className="text-sm font-bold text-gray-900 dark:text-white">Question Details</h3>
            {activeSession.questions.map((q, idx) => (
              <Card key={q.id} className="p-5 space-y-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="w-6 h-6 rounded-full bg-blue-100 dark:bg-blue-900/60 text-blue-700 dark:text-blue-300 text-xs font-black flex items-center justify-center">
                      {idx + 1}
                    </span>
                    <h4 className="text-sm font-bold text-gray-900 dark:text-white">{q.title}</h4>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant={q.difficulty.toLowerCase() as any}>{q.difficulty}</Badge>
                    <span className="text-xs font-bold text-amber-500 flex items-center gap-1">
                      <Star className="w-3.5 h-3.5 fill-current" /> {q.selfRating || 3}/5
                    </span>
                  </div>
                </div>

                {q.userNotes && (
                  <div className="p-3 bg-gray-50 dark:bg-slate-800/60 rounded-xl font-mono text-[11px] text-gray-700 dark:text-gray-300 whitespace-pre-wrap mt-2">
                    {q.userNotes}
                  </div>
                )}
              </Card>
            ))}
          </div>
        </motion.div>
      )}

      {/* TAB 4: INTERVIEW HISTORY */}
      {activeTab === 'history' && (
        <div className="space-y-4">
          <h3 className="text-base font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <RotateCcw className="w-4 h-4 text-blue-500" />
            Completed Mock Assessments
          </h3>

          {pastSessions.length === 0 ? (
            <Card className="p-12 text-center text-gray-500 dark:text-gray-400">
              <Timer className="w-10 h-10 mx-auto mb-2 text-gray-400 opacity-50" />
              <p className="text-sm font-bold">No mock interviews completed yet.</p>
              <p className="text-xs mt-1">Start your first 45-minute timed simulation to test your skills!</p>
              <button
                onClick={() => setActiveTab('setup')}
                className="mt-4 px-5 py-2 rounded-xl bg-blue-600 text-white font-bold text-xs"
              >
                Launch Mock Interview
              </button>
            </Card>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {pastSessions.map((session) => (
                <Card key={session.id} className="p-5 space-y-3 border hover:border-blue-300 transition-colors">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-black uppercase px-2.5 py-0.5 rounded-md bg-blue-50 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400">
                      {session.mode} • {session.difficultyFilter}
                    </span>
                    <span className="text-xs text-gray-400">
                      {new Date(session.startedAt).toLocaleDateString()}
                    </span>
                  </div>

                  <div className="flex items-center justify-between text-xs pt-1">
                    <div>
                      <span className="text-gray-400 block text-[10px]">Score</span>
                      <span className="font-black text-gray-900 dark:text-white text-sm">
                        {session.score} / {session.questionCount} Solved
                      </span>
                    </div>

                    <div>
                      <span className="text-gray-400 block text-[10px]">Time Limit</span>
                      <span className="font-bold text-gray-700 dark:text-gray-300">
                        {session.timeLimitMinutes} mins
                      </span>
                    </div>

                    <div>
                      <span className="text-gray-400 block text-[10px]">XP Awarded</span>
                      <span className="font-extrabold text-amber-500">
                        +{session.xpAwarded} XP
                      </span>
                    </div>
                  </div>

                  <div className="pt-2 border-t border-gray-100 dark:border-slate-800 flex justify-end">
                    <button
                      onClick={() => {
                        setActiveSession(session);
                        setActiveTab('results');
                      }}
                      className="text-xs font-bold text-blue-600 dark:text-blue-400 hover:underline flex items-center gap-1"
                    >
                      <span>View Breakdown</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default MockInterviewPage;
