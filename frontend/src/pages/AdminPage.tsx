import React, { useState, useEffect } from 'react';
import { api } from '../lib/api';
import {
  User, TaskSheet, Topic, AdminOverviewStats, EngagementTrend,
  TopicDropOff, SheetAnalytics, AuditLog, Announcement
} from '../types';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import { useToast } from '../components/ui/Toast';
import AnimatedNumber from '../components/common/AnimatedNumber';
import {
  Settings, Users, FileText, Plus, Check, BarChart3, ShieldAlert,
  Megaphone, ScrollText, Calendar, TrendingUp, AlertTriangle,
  UserX, UserCheck, Clock, Search, Trash2, Send,
  Award
} from 'lucide-react';
import { clsx } from 'clsx';
import { motion, AnimatePresence } from 'framer-motion';
import {
  AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer
} from 'recharts';

const AdminPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<
    'analytics' | 'members' | 'sheets' | 'create-sheet' | 'create-task' | 'broadcast' | 'audit-log'
  >('analytics');

  // Common Data State
  const [members, setMembers] = useState<User[]>([]);
  const [sheets, setSheets] = useState<TaskSheet[]>([]);
  const [topics, setTopics] = useState<Topic[]>([]);
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);

  // Analytics State
  const [overview, setOverview] = useState<AdminOverviewStats | null>(null);
  const [engagementDays, setEngagementDays] = useState<number>(14);
  const [engagementTrends, setEngagementTrends] = useState<EngagementTrend[]>([]);
  const [topicDropOffs, setTopicDropOffs] = useState<TopicDropOff[]>([]);
  const [selectedSheetId, setSelectedSheetId] = useState<number | null>(null);
  const [sheetAnalytics, setSheetAnalytics] = useState<SheetAnalytics | null>(null);

  // Audit Logs State
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [auditFilter, setAuditFilter] = useState<string>('ALL');
  const [auditSearch, setAuditSearch] = useState<string>('');

  // Member Moderation Modal State
  const [moderatingMember, setModeratingMember] = useState<User | null>(null);
  const [modStatus, setModStatus] = useState<'ACTIVE' | 'MUTED' | 'BANNED'>('ACTIVE');
  const [modMuteHours, setModMuteHours] = useState<number>(24);
  const [modReason, setModReason] = useState<string>('');
  const [modLoading, setModLoading] = useState<boolean>(false);
  const [memberSearch, setMemberSearch] = useState<string>('');

  // Announcement Form State
  const [broadcastForm, setBroadcastForm] = useState({
    title: '',
    message: '',
    priority: 'NORMAL' as 'NORMAL' | 'HIGH' | 'URGENT',
    expiresInDays: 7
  });
  const [broadcastLoading, setBroadcastLoading] = useState(false);

  // Sheet & Task Form States
  const [sheetForm, setSheetForm] = useState({
    title: '',
    description: '',
    startDate: '',
    endDate: '',
    sheetType: 'DAILY'
  });
  const [taskForm, setTaskForm] = useState({
    taskSheetId: '',
    title: '',
    description: '',
    difficulty: 'EASY',
    topicId: '',
    platformLink: '',
    xpReward: 100
  });

  const { show } = useToast();

  // Data Fetching
  const fetchMembers = async () => {
    try { const data = await api.getMembers(); setMembers(data); } catch { show('Error fetching members', 'error'); }
  };

  const fetchSheets = async () => {
    try {
      const data = await api.getTaskSheets();
      setSheets(data);
      if (data.length > 0 && !selectedSheetId) {
        setSelectedSheetId(data[0].id);
      }
    } catch {
      show('Error fetching sheets', 'error');
    }
  };

  const fetchTopics = async () => {
    try { const data = await api.getTopics(); setTopics(data); } catch (e) { console.error(e); }
  };

  const fetchAnalytics = async () => {
    try {
      const [ov, eng, drop] = await Promise.all([
        api.getAdminOverview(),
        api.getAdminEngagement(engagementDays),
        api.getAdminTopicDropoff()
      ]);
      setOverview(ov);
      setEngagementTrends(eng);
      setTopicDropOffs(drop);
    } catch (e) {
      console.error('Failed to load admin analytics', e);
    }
  };

  const fetchSheetAnalytics = async (sheetId: number) => {
    try {
      const data = await api.getAdminSheetAnalytics(sheetId);
      setSheetAnalytics(data);
    } catch (e) {
      console.error('Failed to load sheet analytics', e);
    }
  };

  const fetchAuditLogs = async () => {
    try {
      const logs = await api.getAdminAuditLogs();
      setAuditLogs(logs);
    } catch (e) {
      console.error('Failed to load audit logs', e);
    }
  };

  const fetchAnnouncements = async () => {
    try {
      const data = await api.getAdminAnnouncements();
      setAnnouncements(data);
    } catch (e) {
      console.error('Failed to load announcements', e);
    }
  };

  useEffect(() => {
    fetchMembers();
    fetchSheets();
    fetchTopics();
    fetchAnalytics();
    fetchAuditLogs();
    fetchAnnouncements();
  }, []);

  useEffect(() => {
    if (activeTab === 'analytics') {
      api.getAdminEngagement(engagementDays).then(setEngagementTrends).catch(() => {});
    }
  }, [engagementDays, activeTab]);

  useEffect(() => {
    if (selectedSheetId) {
      fetchSheetAnalytics(selectedSheetId);
    }
  }, [selectedSheetId]);

  // Actions
  const handleRoleChange = async (userId: number, role: string) => {
    try {
      await api.updateMemberRole(userId, role);
      show('Role updated successfully', 'success');
      fetchMembers();
      fetchAuditLogs();
    } catch (e: any) {
      show(e.message || 'Error updating role', 'error');
    }
  };

  const openModerationModal = (member: User) => {
    setModeratingMember(member);
    setModStatus((member.status as any) || 'ACTIVE');
    setModMuteHours(24);
    setModReason(member.moderationReason || '');
  };

  const handleSaveModeration = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!moderatingMember) return;
    setModLoading(true);
    try {
      await api.updateMemberStatus(moderatingMember.id, {
        status: modStatus,
        muteDurationHours: modStatus === 'MUTED' ? modMuteHours : undefined,
        reason: modReason.trim() || undefined
      });
      show(`User status updated to ${modStatus}`, 'success');
      setModeratingMember(null);
      fetchMembers();
      fetchAuditLogs();
    } catch (e: any) {
      show(e.message || 'Error updating member status', 'error');
    } finally {
      setModLoading(false);
    }
  };

  const handleBroadcast = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!broadcastForm.title.trim() || !broadcastForm.message.trim()) {
      show('Please provide title and message', 'error');
      return;
    }
    setBroadcastLoading(true);
    try {
      await api.broadcastAnnouncement(broadcastForm);
      show('Announcement broadcasted successfully!', 'success');
      setBroadcastForm({ title: '', message: '', priority: 'NORMAL', expiresInDays: 7 });
      fetchAnnouncements();
      fetchAuditLogs();
    } catch (e: any) {
      show(e.message || 'Failed to broadcast announcement', 'error');
    } finally {
      setBroadcastLoading(false);
    }
  };

  const handleDeleteAnnouncement = async (id: number) => {
    if (!confirm('Are you sure you want to delete this announcement?')) return;
    try {
      await api.deleteAnnouncement(id);
      show('Announcement removed', 'success');
      fetchAnnouncements();
      fetchAuditLogs();
    } catch (e: any) {
      show(e.message || 'Error deleting announcement', 'error');
    }
  };

  const handleDeleteSheet = async (id: number) => {
    if (!confirm('Are you sure you want to delete this sheet and all its tasks?')) return;
    try {
      await api.deleteTaskSheet(id);
      show('Task sheet deleted successfully', 'success');
      fetchSheets();
      fetchAuditLogs();
      fetchAnalytics();
    } catch (e: any) {
      show(e.message || 'Error deleting sheet', 'error');
    }
  };

  const handleCreateSheet = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sheetForm.title.trim()) {
      show('Please enter a sheet title', 'error');
      return;
    }
    if (!sheetForm.startDate || !sheetForm.endDate) {
      show('Please select valid start and end dates', 'error');
      return;
    }
    try {
      await api.createTaskSheet({
        title: sheetForm.title.trim(),
        description: sheetForm.description.trim() || sheetForm.title.trim(),
        startDate: sheetForm.startDate,
        endDate: sheetForm.endDate,
        sheetType: sheetForm.sheetType,
      });
      show('Task sheet created successfully!', 'success');
      setSheetForm({ title: '', description: '', startDate: '', endDate: '', sheetType: 'DAILY' });
      fetchSheets();
      fetchAuditLogs();
      setActiveTab('sheets');
    } catch (e: any) {
      show(e.message || 'Error creating sheet', 'error');
    }
  };

  const handleCreateTask = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!taskForm.taskSheetId) {
      show('Please select a task sheet', 'error');
      return;
    }
    if (!taskForm.topicId) {
      show('Please select a topic', 'error');
      return;
    }
    if (!taskForm.title.trim()) {
      show('Please enter a task title', 'error');
      return;
    }
    try {
      await api.createTask({
        taskSheetId: Number(taskForm.taskSheetId),
        topicId: Number(taskForm.topicId),
        title: taskForm.title.trim(),
        description: taskForm.description.trim(),
        difficulty: taskForm.difficulty,
        platformLink: taskForm.platformLink.trim() || 'https://leetcode.com',
        xpReward: Number(taskForm.xpReward) || 100,
      });
      show('Task created successfully!', 'success');
      setTaskForm({ ...taskForm, title: '', description: '', platformLink: '', xpReward: 100 });
      fetchSheets();
      fetchAuditLogs();
      setActiveTab('sheets');
    } catch (e: any) {
      show(e.message || 'Error creating task', 'error');
    }
  };

  // Filtered members & audit logs
  const filteredMembers = members.filter(m =>
    m.name?.toLowerCase().includes(memberSearch.toLowerCase()) ||
    m.email?.toLowerCase().includes(memberSearch.toLowerCase())
  );

  const filteredAuditLogs = auditLogs.filter(log => {
    const matchesFilter = auditFilter === 'ALL' || log.action === auditFilter;
    const matchesSearch = !auditSearch ||
      log.adminEmail?.toLowerCase().includes(auditSearch.toLowerCase()) ||
      log.details?.toLowerCase().includes(auditSearch.toLowerCase()) ||
      log.action?.toLowerCase().includes(auditSearch.toLowerCase());
    return matchesFilter && matchesSearch;
  });

  return (
    <div className="space-y-8 max-w-6xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="px-2.5 py-0.5 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-600 dark:text-blue-400 text-xs font-bold uppercase tracking-wider inline-flex items-center gap-1">
              <Settings className="w-3.5 h-3.5" /> Administration Suite
            </span>
          </div>
          <h1 className="text-3xl font-black tracking-tight text-gray-900 dark:text-white">
            Admin Control Center
          </h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">
            Monitor engagement metrics, moderate member access, manage curricula, and audit platform activity.
          </p>
        </div>

        {overview && (
          <div className="flex items-center gap-3 bg-white dark:bg-slate-800/80 border border-gray-200 dark:border-slate-700/60 p-2.5 rounded-2xl shadow-xs">
            <div className="px-3 py-1 text-center border-r border-gray-200 dark:border-slate-700">
              <div className="text-[10px] uppercase font-bold text-gray-400">DAU</div>
              <div className="text-base font-black text-emerald-600 dark:text-emerald-400">
                <AnimatedNumber value={overview.dailyActiveUsers} />
              </div>
            </div>
            <div className="px-3 py-1 text-center border-r border-gray-200 dark:border-slate-700">
              <div className="text-[10px] uppercase font-bold text-gray-400">WAU</div>
              <div className="text-base font-black text-indigo-600 dark:text-indigo-400">
                <AnimatedNumber value={overview.weeklyActiveUsers} />
              </div>
            </div>
            <div className="px-3 py-1 text-center">
              <div className="text-[10px] uppercase font-bold text-gray-400">Total Solves</div>
              <div className="text-base font-black text-blue-600 dark:text-blue-400">
                <AnimatedNumber value={overview.totalCompletions} />
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Tabs */}
      <div className="flex overflow-x-auto bg-gray-100 dark:bg-slate-800/90 p-1.5 rounded-2xl gap-1 border border-gray-200/50 dark:border-slate-700/50">
        {[
          { id: 'analytics', label: 'Analytics', icon: BarChart3 },
          { id: 'members', label: 'Members & Moderation', icon: Users },
          { id: 'sheets', label: 'Task Sheets', icon: FileText },
          { id: 'create-sheet', label: 'Create Sheet', icon: Plus },
          { id: 'create-task', label: 'Add Task', icon: Plus },
          { id: 'broadcast', label: 'Announcements', icon: Megaphone },
          { id: 'audit-log', label: 'Audit Trail', icon: ScrollText },
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as any)}
            className={clsx(
              "flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold text-sm transition-all whitespace-nowrap",
              activeTab === tab.id
                ? "bg-white dark:bg-slate-700 text-blue-600 dark:text-blue-400 shadow-sm"
                : "text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-white/40 dark:hover:bg-slate-700/40"
            )}
          >
            <tab.icon className="w-4 h-4" /> {tab.label}
          </button>
        ))}
      </div>

      {/* TAB CONTENT */}
      <div>
        {/* ANALYTICS TAB */}
        {activeTab === 'analytics' && (
          <div className="space-y-6">
            {/* KPI Cards */}
            {overview && (
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
                <Card className="p-4 bg-gradient-to-br from-blue-500/10 via-transparent to-transparent border-blue-500/20">
                  <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1 flex items-center justify-between">
                    <span>Members</span>
                    <Users className="w-4 h-4 text-blue-500" />
                  </div>
                  <div className="text-2xl font-black text-gray-900 dark:text-white">
                    <AnimatedNumber value={overview.totalUsers} />
                  </div>
                  <div className="text-[11px] text-blue-600 dark:text-blue-400 mt-1 font-semibold">
                    {overview.activeStreaksCount} active streaks
                  </div>
                </Card>

                <Card className="p-4 bg-gradient-to-br from-emerald-500/10 via-transparent to-transparent border-emerald-500/20">
                  <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1 flex items-center justify-between">
                    <span>Active Today</span>
                    <TrendingUp className="w-4 h-4 text-emerald-500" />
                  </div>
                  <div className="text-2xl font-black text-gray-900 dark:text-white">
                    <AnimatedNumber value={overview.dailyActiveUsers} />
                  </div>
                  <div className="text-[11px] text-emerald-600 dark:text-emerald-400 mt-1 font-semibold">
                    {Math.round((overview.dailyActiveUsers / Math.max(1, overview.totalUsers)) * 100)}% of total
                  </div>
                </Card>

                <Card className="p-4 bg-gradient-to-br from-indigo-500/10 via-transparent to-transparent border-indigo-500/20">
                  <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1 flex items-center justify-between">
                    <span>Active Week</span>
                    <Calendar className="w-4 h-4 text-indigo-500" />
                  </div>
                  <div className="text-2xl font-black text-gray-900 dark:text-white">
                    <AnimatedNumber value={overview.weeklyActiveUsers} />
                  </div>
                  <div className="text-[11px] text-indigo-600 dark:text-indigo-400 mt-1 font-semibold">
                    WAU / Total: {Math.round((overview.weeklyActiveUsers / Math.max(1, overview.totalUsers)) * 100)}%
                  </div>
                </Card>

                <Card className="p-4 bg-gradient-to-br from-purple-500/10 via-transparent to-transparent border-purple-500/20">
                  <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1 flex items-center justify-between">
                    <span>Total Solves</span>
                    <Check className="w-4 h-4 text-purple-500" />
                  </div>
                  <div className="text-2xl font-black text-gray-900 dark:text-white">
                    <AnimatedNumber value={overview.totalCompletions} />
                  </div>
                  <div className="text-[11px] text-purple-600 dark:text-purple-400 mt-1 font-semibold">
                    across {overview.totalTasks} tasks
                  </div>
                </Card>

                <Card className="p-4 bg-gradient-to-br from-amber-500/10 via-transparent to-transparent border-amber-500/20">
                  <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1 flex items-center justify-between">
                    <span>Average XP</span>
                    <Award className="w-4 h-4 text-amber-500" />
                  </div>
                  <div className="text-2xl font-black text-gray-900 dark:text-white">
                    <AnimatedNumber value={overview.averageXp} />
                  </div>
                  <div className="text-[11px] text-amber-600 dark:text-amber-400 mt-1 font-semibold">
                    per member
                  </div>
                </Card>

                <Card className="p-4 bg-gradient-to-br from-cyan-500/10 via-transparent to-transparent border-cyan-500/20">
                  <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1 flex items-center justify-between">
                    <span>Task Sheets</span>
                    <FileText className="w-4 h-4 text-cyan-500" />
                  </div>
                  <div className="text-2xl font-black text-gray-900 dark:text-white">
                    <AnimatedNumber value={overview.totalTaskSheets} />
                  </div>
                  <div className="text-[11px] text-cyan-600 dark:text-cyan-400 mt-1 font-semibold">
                    curated modules
                  </div>
                </Card>
              </div>
            )}

            {/* Engagement Trend Graph */}
            <Card className="p-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                <div>
                  <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
                    <TrendingUp className="w-5 h-5 text-blue-500" /> User Engagement & Problem Solves Trend
                  </h3>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                    Daily active members and problem completion volume over time
                  </p>
                </div>

                <div className="flex items-center gap-1 bg-gray-100 dark:bg-slate-800 p-1 rounded-xl text-xs font-bold">
                  {[7, 14, 30].map(days => (
                    <button
                      key={days}
                      onClick={() => setEngagementDays(days)}
                      className={clsx(
                        "px-3 py-1.5 rounded-lg transition-all",
                        engagementDays === days
                          ? "bg-white dark:bg-slate-700 text-blue-600 dark:text-blue-400 shadow-xs"
                          : "text-gray-500 hover:text-gray-900 dark:hover:text-white"
                      )}
                    >
                      Last {days} Days
                    </button>
                  ))}
                </div>
              </div>

              <div className="h-72 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={engagementTrends} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    <defs>
                      <linearGradient id="activeUsersGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.4}/>
                        <stop offset="95%" stopColor="#3b82f6" stopOpacity={0.0}/>
                      </linearGradient>
                      <linearGradient id="completionsGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#10b981" stopOpacity={0.4}/>
                        <stop offset="95%" stopColor="#10b981" stopOpacity={0.0}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                    <XAxis
                      dataKey="date"
                      tickFormatter={(d) => {
                        const parts = d.split('-');
                        return parts.length === 3 ? `${parts[1]}/${parts[2]}` : d;
                      }}
                      tick={{ fill: '#888', fontSize: 11 }}
                    />
                    <YAxis tick={{ fill: '#888', fontSize: 11 }} allowDecimals={false} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: '#1e293b',
                        borderColor: '#334155',
                        borderRadius: '12px',
                        color: '#fff',
                        fontSize: '12px'
                      }}
                    />
                    <Area
                      type="monotone"
                      dataKey="activeUsers"
                      name="Active Users"
                      stroke="#3b82f6"
                      strokeWidth={2.5}
                      fillOpacity={1}
                      fill="url(#activeUsersGrad)"
                    />
                    <Area
                      type="monotone"
                      dataKey="completionsCount"
                      name="Problem Solves"
                      stroke="#10b981"
                      strokeWidth={2.5}
                      fillOpacity={1}
                      fill="url(#completionsGrad)"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </Card>

            {/* Topic Drop-Off Analysis & Sheet Funnel */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Topic Drop-off Rates */}
              <Card className="p-6">
                <div className="mb-4">
                  <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
                    <AlertTriangle className="w-5 h-5 text-amber-500" /> Topic Drop-Off & Difficulty Analysis
                  </h3>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                    Topics ranked by lowest completion rate (highest learner drop-off)
                  </p>
                </div>

                <div className="h-64 w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                      data={topicDropOffs}
                      layout="vertical"
                      margin={{ top: 5, right: 20, left: 40, bottom: 5 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" opacity={0.15} horizontal={false} />
                      <XAxis type="number" domain={[0, 100]} unit="%" tick={{ fill: '#888', fontSize: 10 }} />
                      <YAxis
                        type="category"
                        dataKey="topicName"
                        tick={{ fill: '#888', fontSize: 11 }}
                        width={90}
                      />
                      <Tooltip
                        contentStyle={{
                          backgroundColor: '#1e293b',
                          borderColor: '#334155',
                          borderRadius: '12px',
                          color: '#fff',
                          fontSize: '12px'
                        }}
                        formatter={(val: any) => [`${val}%`, 'Drop-off Rate']}
                      />
                      <Bar dataKey="dropOffRate" fill="#f59e0b" radius={[0, 6, 6, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </Card>

              {/* Sheet Funnel Inspector */}
              <Card className="p-6 flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between gap-2 mb-4">
                    <div>
                      <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
                        <FileText className="w-5 h-5 text-blue-500" /> Sheet Funnel Inspector
                      </h3>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                        Track question drop-off within a specific curriculum module
                      </p>
                    </div>

                    <select
                      className="bg-gray-100 dark:bg-slate-800 text-xs font-bold rounded-xl px-3 py-2 border border-gray-200 dark:border-slate-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500"
                      value={selectedSheetId || ''}
                      onChange={(e) => setSelectedSheetId(Number(e.target.value))}
                    >
                      {sheets.map(s => (
                        <option key={s.id} value={s.id}>{s.title}</option>
                      ))}
                    </select>
                  </div>

                  {sheetAnalytics ? (
                    <div className="space-y-4">
                      <div className="flex items-center justify-between p-3 rounded-xl bg-gray-50 dark:bg-slate-800/60 border border-gray-200/60 dark:border-slate-700/60 text-xs">
                        <div>
                          <span className="text-gray-500">Total Tasks: </span>
                          <span className="font-bold text-gray-900 dark:text-white">{sheetAnalytics.totalTasks}</span>
                        </div>
                        <div>
                          <span className="text-gray-500">Unique Learners: </span>
                          <span className="font-bold text-blue-600 dark:text-blue-400">{sheetAnalytics.uniqueCompletedUsers}</span>
                        </div>
                        <div>
                          <span className="text-gray-500">Avg Completion: </span>
                          <span className="font-bold text-emerald-600 dark:text-emerald-400">{sheetAnalytics.overallCompletionRate}%</span>
                        </div>
                      </div>

                      <div className="space-y-2.5 max-h-52 overflow-y-auto pr-1">
                        {sheetAnalytics.questionStats.map((q, idx) => (
                          <div key={q.taskId} className="p-2.5 rounded-xl border border-gray-100 dark:border-slate-800 bg-white dark:bg-slate-900/60 text-xs">
                            <div className="flex items-center justify-between mb-1">
                              <span className="font-bold text-gray-800 dark:text-gray-200 truncate max-w-[220px]">
                                {idx + 1}. {q.taskTitle}
                              </span>
                              <div className="flex items-center gap-1.5 flex-shrink-0">
                                <span className={clsx(
                                  "px-1.5 py-0.5 rounded text-[10px] font-extrabold",
                                  q.difficulty === 'EASY' ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300" :
                                  q.difficulty === 'MEDIUM' ? "bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300" :
                                  "bg-rose-100 text-rose-700 dark:bg-rose-950/60 dark:text-rose-300"
                                )}>
                                  {q.difficulty}
                                </span>
                                <span className="font-black text-gray-900 dark:text-white">{q.completionRate}%</span>
                              </div>
                            </div>
                            <div className="w-full bg-gray-100 dark:bg-slate-800 rounded-full h-1.5 overflow-hidden">
                              <div
                                className="bg-blue-600 h-1.5 rounded-full transition-all"
                                style={{ width: `${Math.min(100, q.completionRate)}%` }}
                              />
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  ) : (
                    <div className="py-12 text-center text-xs text-gray-400">Select a task sheet above to inspect question drop-off.</div>
                  )}
                </div>
              </Card>
            </div>
          </div>
        )}

        {/* MEMBERS & MODERATION TAB */}
        {activeTab === 'members' && (
          <Card className="overflow-hidden">
            <div className="p-4 border-b border-gray-200 dark:border-slate-700/80 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="relative flex-1 max-w-md">
                <Search className="w-4 h-4 absolute left-3 top-3 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search members by name or email..."
                  className="w-full pl-9 pr-3 py-2 bg-gray-50 dark:bg-slate-800/80 border border-gray-200 dark:border-slate-700 rounded-xl text-sm dark:text-white focus:ring-2 focus:ring-blue-500"
                  value={memberSearch}
                  onChange={e => setMemberSearch(e.target.value)}
                />
              </div>

              <div className="text-xs text-gray-500 dark:text-gray-400">
                Showing {filteredMembers.length} members
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-50/70 dark:bg-slate-800/70 border-b border-gray-200 dark:border-slate-700 text-gray-500 dark:text-gray-400 text-xs uppercase font-bold tracking-wider">
                    <th className="py-3 px-4">Member</th>
                    <th className="py-3 px-4">Email</th>
                    <th className="py-3 px-4">Joined</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4">Role</th>
                    <th className="py-3 px-4 text-right">Moderation</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-slate-800 text-sm">
                  {filteredMembers.map(member => {
                    const isMuted = member.status === 'MUTED';
                    const isBanned = member.status === 'BANNED';
                    const isPrimaryAdmin = member.email === 'hi2gauravgb@gmail.com';

                    return (
                      <tr key={member.id} className="hover:bg-gray-50/60 dark:hover:bg-slate-800/40 transition-colors">
                        <td className="py-3 px-4 flex items-center gap-3">
                          <img src={member.avatarUrl} alt="" className="w-8 h-8 rounded-full border border-gray-200 dark:border-slate-700 object-cover" />
                          <div>
                            <div className="font-bold dark:text-white flex items-center gap-1.5">
                              {member.name}
                              {member.level && (
                                <span className="text-[10px] px-1.5 py-0.2 rounded bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400 font-extrabold">
                                  Lv.{member.level}
                                </span>
                              )}
                            </div>
                            <div className="text-[11px] text-gray-400">Streak: {member.currentStreak}d • {member.totalXp} XP</div>
                          </div>
                        </td>

                        <td className="py-3 px-4 text-xs text-gray-500 dark:text-gray-400 font-mono">
                          {member.email}
                        </td>

                        <td className="py-3 px-4 text-xs text-gray-500 dark:text-gray-400">
                          {new Date(member.createdAt).toLocaleDateString()}
                        </td>

                        <td className="py-3 px-4">
                          {isBanned ? (
                            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-rose-100 text-rose-700 dark:bg-rose-950/60 dark:text-rose-300">
                              <UserX className="w-3.5 h-3.5" /> Banned
                            </span>
                          ) : isMuted ? (
                            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300">
                              <Clock className="w-3.5 h-3.5" /> Muted
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300">
                              <UserCheck className="w-3.5 h-3.5" /> Active
                            </span>
                          )}
                        </td>

                        <td className="py-3 px-4">
                          <select
                            disabled={isPrimaryAdmin}
                            className="bg-gray-100 dark:bg-slate-800 text-xs font-semibold rounded-lg px-2.5 py-1 border-0 focus:ring-2 focus:ring-blue-500 dark:text-white disabled:opacity-50"
                            value={member.role}
                            onChange={(e) => handleRoleChange(member.id, e.target.value)}
                          >
                            <option value="ROLE_USER">User</option>
                            <option value="ROLE_ADMIN">Admin</option>
                          </select>
                        </td>

                        <td className="py-3 px-4 text-right">
                          <button
                            disabled={isPrimaryAdmin}
                            onClick={() => openModerationModal(member)}
                            className="px-3 py-1.5 text-xs font-bold rounded-xl border border-gray-200 dark:border-slate-700 hover:bg-gray-100 dark:hover:bg-slate-700 text-gray-700 dark:text-gray-300 transition-colors disabled:opacity-40"
                          >
                            Moderate
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </Card>
        )}

        {/* TASK SHEETS TAB */}
        {activeTab === 'sheets' && (
          <Card className="overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-50/70 dark:bg-slate-800/70 border-b border-gray-200 dark:border-slate-700 text-gray-500 dark:text-gray-400 text-xs uppercase font-bold tracking-wider">
                    <th className="py-3 px-4">Title</th>
                    <th className="py-3 px-4">Type</th>
                    <th className="py-3 px-4">Dates</th>
                    <th className="py-3 px-4 text-center">Tasks</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-slate-800 text-sm">
                  {sheets.map(sheet => (
                    <tr key={sheet.id} className="hover:bg-gray-50/60 dark:hover:bg-slate-800/40 transition-colors">
                      <td className="py-3 px-4 font-bold text-gray-900 dark:text-white">{sheet.title}</td>
                      <td className="py-3 px-4">
                        <Badge variant="default">{sheet.sheetType}</Badge>
                      </td>
                      <td className="py-3 px-4 text-xs text-gray-500 dark:text-gray-400">
                        {sheet.startDate} to {sheet.endDate}
                      </td>
                      <td className="py-3 px-4 text-center text-xs font-bold text-gray-700 dark:text-gray-300">
                        {sheet.tasks?.length || 0}
                      </td>
                      <td className="py-3 px-4 text-right">
                        <button
                          onClick={() => handleDeleteSheet(sheet.id)}
                          className="text-rose-500 hover:text-rose-700 text-xs font-bold p-1 rounded-lg hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        )}

        {/* CREATE SHEET TAB */}
        {activeTab === 'create-sheet' && (
          <Card className="p-6 max-w-2xl mx-auto">
            <h2 className="text-xl font-bold dark:text-white mb-6 flex items-center gap-2">
              <Plus className="w-5 h-5 text-blue-500" /> Create New Task Sheet
            </h2>
            <form onSubmit={handleCreateSheet} className="space-y-4">
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">Title</label>
                <input required type="text" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={sheetForm.title} onChange={e => setSheetForm({...sheetForm, title: e.target.value})} placeholder="e.g. Week 4: Dynamic Programming Mastery" />
              </div>
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">Description</label>
                <textarea required rows={3} className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={sheetForm.description} onChange={e => setSheetForm({...sheetForm, description: e.target.value})} placeholder="What concepts this sheet covers..." />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">Start Date</label>
                  <input required type="date" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={sheetForm.startDate} onChange={e => setSheetForm({...sheetForm, startDate: e.target.value})} />
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">End Date</label>
                  <input required type="date" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={sheetForm.endDate} onChange={e => setSheetForm({...sheetForm, endDate: e.target.value})} />
                </div>
              </div>
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">Type</label>
                <select className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={sheetForm.sheetType} onChange={e => setSheetForm({...sheetForm, sheetType: e.target.value})}>
                  <option value="DAILY">DAILY</option>
                  <option value="WEEKLY">WEEKLY</option>
                </select>
              </div>
              <button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 rounded-xl mt-4 transition-colors">
                Publish Sheet
              </button>
            </form>
          </Card>
        )}

        {/* CREATE TASK TAB */}
        {activeTab === 'create-task' && (
          <Card className="p-6 max-w-2xl mx-auto">
            <h2 className="text-xl font-bold dark:text-white mb-6 flex items-center gap-2">
              <Plus className="w-5 h-5 text-emerald-500" /> Add Task to Sheet
            </h2>
            <form onSubmit={handleCreateTask} className="space-y-4">
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">Select Task Sheet</label>
                <select required className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={taskForm.taskSheetId} onChange={e => setTaskForm({...taskForm, taskSheetId: e.target.value})}>
                  <option value="">-- Select Sheet --</option>
                  {sheets.map(s => <option key={s.id} value={s.id}>{s.title}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">Task Title</label>
                <input required type="text" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={taskForm.title} onChange={e => setTaskForm({...taskForm, title: e.target.value})} placeholder="e.g. 3Sum Closest" />
              </div>
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">Description / Hints</label>
                <textarea required rows={2} className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={taskForm.description} onChange={e => setTaskForm({...taskForm, description: e.target.value})} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">Difficulty</label>
                  <select className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={taskForm.difficulty} onChange={e => setTaskForm({...taskForm, difficulty: e.target.value})}>
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">Topic</label>
                  <select required className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={taskForm.topicId} onChange={e => setTaskForm({...taskForm, topicId: e.target.value})}>
                    <option value="">-- Topic --</option>
                    {topics.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-3 gap-4">
                <div className="col-span-2">
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">Platform Link</label>
                  <input required type="url" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={taskForm.platformLink} onChange={e => setTaskForm({...taskForm, platformLink: e.target.value})} />
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400 mb-1">XP Reward</label>
                  <input required type="number" min="1" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-xl p-2.5 dark:text-white" value={taskForm.xpReward} onChange={e => setTaskForm({...taskForm, xpReward: Number(e.target.value)})} />
                </div>
              </div>
              <button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-3 rounded-xl mt-4 transition-colors flex items-center justify-center gap-2">
                <Check className="w-5 h-5" /> Add Task
              </button>
            </form>
          </Card>
        )}

        {/* ANNOUNCEMENTS TAB */}
        {activeTab === 'broadcast' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Create Announcement */}
            <Card className="p-6 lg:col-span-1">
              <h2 className="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
                <Megaphone className="w-5 h-5 text-indigo-500" /> New Broadcast
              </h2>
              <form onSubmit={handleBroadcast} className="space-y-4">
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 mb-1">Title</label>
                  <input
                    required
                    type="text"
                    className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-200 dark:border-slate-700 rounded-xl p-2.5 text-sm dark:text-white"
                    placeholder="e.g. Weekly Contest Tonight at 9 PM"
                    value={broadcastForm.title}
                    onChange={e => setBroadcastForm({...broadcastForm, title: e.target.value})}
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 mb-1">Priority</label>
                  <select
                    className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-200 dark:border-slate-700 rounded-xl p-2.5 text-sm dark:text-white"
                    value={broadcastForm.priority}
                    onChange={e => setBroadcastForm({...broadcastForm, priority: e.target.value as any})}
                  >
                    <option value="NORMAL">Normal (Info Blue)</option>
                    <option value="HIGH">High (Warning Amber)</option>
                    <option value="URGENT">Urgent (Alert Rose)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 mb-1">Expires In</label>
                  <select
                    className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-200 dark:border-slate-700 rounded-xl p-2.5 text-sm dark:text-white"
                    value={broadcastForm.expiresInDays}
                    onChange={e => setBroadcastForm({...broadcastForm, expiresInDays: Number(e.target.value)})}
                  >
                    <option value={1}>24 Hours</option>
                    <option value={3}>3 Days</option>
                    <option value={7}>7 Days</option>
                    <option value={14}>14 Days</option>
                    <option value={30}>30 Days</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 mb-1">Message</label>
                  <textarea
                    required
                    rows={4}
                    className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-200 dark:border-slate-700 rounded-xl p-2.5 text-sm dark:text-white"
                    placeholder="Detailed announcement content visible to all members on their dashboard..."
                    value={broadcastForm.message}
                    onChange={e => setBroadcastForm({...broadcastForm, message: e.target.value})}
                  />
                </div>

                <button
                  type="submit"
                  disabled={broadcastLoading}
                  className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2.5 rounded-xl transition-all flex items-center justify-center gap-2 shadow-sm"
                >
                  <Send className="w-4 h-4" /> Broadcast to All Members
                </button>
              </form>
            </Card>

            {/* List Announcements */}
            <div className="lg:col-span-2 space-y-4">
              <h2 className="text-lg font-bold text-gray-900 dark:text-white flex items-center justify-between">
                <span>Active & Past Announcements</span>
                <span className="text-xs text-gray-400 font-normal">{announcements.length} total</span>
              </h2>

              {announcements.length === 0 ? (
                <Card className="p-8 text-center text-gray-400 text-sm">
                  No announcements broadcasted yet. Use the form to send an alert to all members.
                </Card>
              ) : (
                <div className="space-y-3">
                  {announcements.map(item => (
                    <Card key={item.id} className="p-4 flex items-start justify-between gap-4">
                      <div>
                        <div className="flex items-center gap-2">
                          <span className={clsx(
                            "text-[10px] uppercase font-bold px-2 py-0.5 rounded-full",
                            item.priority === 'URGENT' ? "bg-rose-100 text-rose-700 dark:bg-rose-900/60 dark:text-rose-300" :
                            item.priority === 'HIGH' ? "bg-amber-100 text-amber-700 dark:bg-amber-900/60 dark:text-amber-300" :
                            "bg-blue-100 text-blue-700 dark:bg-blue-900/60 dark:text-blue-300"
                          )}>
                            {item.priority}
                          </span>
                          <h4 className="font-bold text-sm text-gray-900 dark:text-white">{item.title}</h4>
                        </div>
                        <p className="text-xs text-gray-600 dark:text-gray-300 mt-1 whitespace-pre-wrap">{item.message}</p>
                        <div className="text-[11px] text-gray-400 mt-2">
                          Posted by {item.authorName} • {new Date(item.createdAt).toLocaleDateString()}
                          {item.expiresAt && ` • Expires ${new Date(item.expiresAt).toLocaleDateString()}`}
                        </div>
                      </div>

                      <button
                        onClick={() => handleDeleteAnnouncement(item.id)}
                        className="text-gray-400 hover:text-rose-500 p-1.5 rounded-lg hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors"
                        title="Delete announcement"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </Card>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {/* AUDIT TRAIL TAB */}
        {activeTab === 'audit-log' && (
          <Card className="overflow-hidden">
            <div className="p-4 border-b border-gray-200 dark:border-slate-700/80 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="flex items-center gap-2 flex-1 max-w-md">
                <Search className="w-4 h-4 text-gray-400" />
                <input
                  type="text"
                  placeholder="Filter audit logs..."
                  className="w-full py-1.5 px-3 bg-gray-50 dark:bg-slate-800/80 border border-gray-200 dark:border-slate-700 rounded-xl text-xs dark:text-white focus:ring-2 focus:ring-blue-500"
                  value={auditSearch}
                  onChange={e => setAuditSearch(e.target.value)}
                />
              </div>

              <div className="flex items-center gap-2">
                <span className="text-xs text-gray-400">Action:</span>
                <select
                  className="bg-gray-100 dark:bg-slate-800 text-xs font-semibold rounded-xl px-2.5 py-1.5 border border-gray-200 dark:border-slate-700 dark:text-white"
                  value={auditFilter}
                  onChange={e => setAuditFilter(e.target.value)}
                >
                  <option value="ALL">All Actions</option>
                  <option value="CREATE_SHEET">CREATE_SHEET</option>
                  <option value="DELETE_SHEET">DELETE_SHEET</option>
                  <option value="CREATE_TASK">CREATE_TASK</option>
                  <option value="DELETE_TASK">DELETE_TASK</option>
                  <option value="UPDATE_ROLE">UPDATE_ROLE</option>
                  <option value="STATUS_CHANGE">STATUS_CHANGE</option>
                  <option value="CREATE_ANNOUNCEMENT">CREATE_ANNOUNCEMENT</option>
                  <option value="DELETE_ANNOUNCEMENT">DELETE_ANNOUNCEMENT</option>
                </select>
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-50/70 dark:bg-slate-800/70 border-b border-gray-200 dark:border-slate-700 text-gray-500 dark:text-gray-400 text-xs uppercase font-bold tracking-wider">
                    <th className="py-3 px-4">Timestamp</th>
                    <th className="py-3 px-4">Admin</th>
                    <th className="py-3 px-4">Action</th>
                    <th className="py-3 px-4">Entity</th>
                    <th className="py-3 px-4">Details</th>
                    <th className="py-3 px-4 text-right">IP</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-slate-800 text-xs">
                  {filteredAuditLogs.map(log => {
                    const isDelete = log.action.includes('DELETE');
                    const isStatus = log.action.includes('STATUS');
                    const isCreate = log.action.includes('CREATE');

                    return (
                      <tr key={log.id} className="hover:bg-gray-50/50 dark:hover:bg-slate-800/40">
                        <td className="py-3 px-4 text-gray-400 whitespace-nowrap">
                          {new Date(log.createdAt).toLocaleString()}
                        </td>

                        <td className="py-3 px-4 font-semibold text-gray-900 dark:text-gray-200 whitespace-nowrap">
                          {log.adminName}
                          <div className="text-[10px] text-gray-400 font-mono">{log.adminEmail}</div>
                        </td>

                        <td className="py-3 px-4">
                          <span className={clsx(
                            "px-2 py-0.5 rounded font-black tracking-wider text-[10px] uppercase",
                            isDelete ? "bg-rose-100 text-rose-700 dark:bg-rose-950/60 dark:text-rose-300" :
                            isStatus ? "bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300" :
                            isCreate ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300" :
                            "bg-blue-100 text-blue-700 dark:bg-blue-950/60 dark:text-blue-300"
                          )}>
                            {log.action}
                          </span>
                        </td>

                        <td className="py-3 px-4 text-gray-500 dark:text-gray-400 whitespace-nowrap">
                          {log.entityType} {log.entityId ? `#${log.entityId}` : ''}
                        </td>

                        <td className="py-3 px-4 text-gray-700 dark:text-gray-300 max-w-sm break-words">
                          {log.details}
                        </td>

                        <td className="py-3 px-4 text-right font-mono text-gray-400">
                          {log.ipAddress}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </Card>
        )}
      </div>

      {/* MEMBER MODERATION MODAL */}
      <AnimatePresence>
        {moderatingMember && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-white dark:bg-slate-900 rounded-3xl p-6 max-w-md w-full border border-gray-200 dark:border-slate-800 shadow-2xl"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
                  <ShieldAlert className="w-5 h-5 text-amber-500" /> Moderate Member
                </h3>
                <button
                  onClick={() => setModeratingMember(null)}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 text-sm font-bold"
                >
                  ✕
                </button>
              </div>

              <div className="flex items-center gap-3 p-3 rounded-2xl bg-gray-50 dark:bg-slate-800 mb-4">
                <img src={moderatingMember.avatarUrl} alt="" className="w-10 h-10 rounded-full" />
                <div>
                  <div className="font-bold text-sm text-gray-900 dark:text-white">{moderatingMember.name}</div>
                  <div className="text-xs text-gray-400">{moderatingMember.email}</div>
                </div>
              </div>

              <form onSubmit={handleSaveModeration} className="space-y-4">
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 mb-1">Set Account Status</label>
                  <div className="grid grid-cols-3 gap-2">
                    {[
                      { val: 'ACTIVE', label: 'Active', icon: UserCheck, color: 'text-emerald-600 dark:text-emerald-400' },
                      { val: 'MUTED', label: 'Mute', icon: Clock, color: 'text-amber-600 dark:text-amber-400' },
                      { val: 'BANNED', label: 'Ban', icon: UserX, color: 'text-rose-600 dark:text-rose-400' },
                    ].map(item => (
                      <button
                        type="button"
                        key={item.val}
                        onClick={() => setModStatus(item.val as any)}
                        className={clsx(
                          "p-2.5 rounded-xl border text-xs font-bold flex flex-col items-center gap-1 transition-all",
                          modStatus === item.val
                            ? "border-blue-500 bg-blue-50/50 dark:bg-blue-950/30 text-blue-600 dark:text-blue-400 shadow-xs"
                            : "border-gray-200 dark:border-slate-800 text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-slate-800"
                        )}
                      >
                        <item.icon className={clsx("w-4 h-4", item.color)} />
                        {item.label}
                      </button>
                    ))}
                  </div>
                </div>

                {modStatus === 'MUTED' && (
                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 mb-1">Mute Duration</label>
                    <select
                      className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-200 dark:border-slate-700 rounded-xl p-2.5 text-xs font-semibold dark:text-white"
                      value={modMuteHours}
                      onChange={e => setModMuteHours(Number(e.target.value))}
                    >
                      <option value={12}>12 Hours</option>
                      <option value={24}>24 Hours (1 Day)</option>
                      <option value={48}>48 Hours (2 Days)</option>
                      <option value={168}>7 Days (1 Week)</option>
                    </select>
                  </div>
                )}

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 mb-1">
                    Moderation Note / Reason
                  </label>
                  <textarea
                    rows={3}
                    className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-200 dark:border-slate-700 rounded-xl p-2.5 text-xs dark:text-white"
                    placeholder="Reason visible to user when blocked or logged in audit trail..."
                    value={modReason}
                    onChange={e => setModReason(e.target.value)}
                  />
                </div>

                <div className="flex items-center justify-end gap-2 pt-2">
                  <button
                    type="button"
                    onClick={() => setModeratingMember(null)}
                    className="px-4 py-2 text-xs font-bold rounded-xl text-gray-500 hover:bg-gray-100 dark:hover:bg-slate-800"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={modLoading}
                    className="px-5 py-2 text-xs font-bold rounded-xl bg-blue-600 hover:bg-blue-700 text-white transition-colors"
                  >
                    {modLoading ? 'Saving...' : 'Confirm Status'}
                  </button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default AdminPage;
