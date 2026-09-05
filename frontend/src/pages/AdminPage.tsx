import React, { useState, useEffect } from 'react';
import { api } from '../lib/api';
import { User, TaskSheet, Topic } from '../types';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import { useToast } from '../components/ui/Toast';
import { Settings, Users, FileText, Plus, Check } from 'lucide-react';
import { clsx } from 'clsx';

const AdminPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'members' | 'sheets' | 'create-sheet' | 'create-task'>('members');
  const [members, setMembers] = useState<User[]>([]);
  const [sheets, setSheets] = useState<TaskSheet[]>([]);
  const [topics, setTopics] = useState<Topic[]>([]);
  const { show } = useToast();

  const fetchMembers = async () => {
    try { const data = await api.getMembers(); setMembers(data); } catch (e) { show('Error fetching members', 'error'); }
  };

  const fetchSheets = async () => {
    try { const data = await api.getTaskSheets(); setSheets(data); } catch (e) { show('Error fetching sheets', 'error'); }
  };

  const fetchTopics = async () => {
    try { const data = await api.getTopics(); setTopics(data); } catch (e) { console.error(e); }
  };

  useEffect(() => {
    fetchMembers();
    fetchSheets();
    fetchTopics();
  }, []);

  const handleRoleChange = async (userId: number, role: string) => {
    try {
      await api.updateMemberRole(userId, role);
      show('Role updated successfully', 'success');
      fetchMembers();
    } catch (e) {
      show('Error updating role', 'error');
    }
  };

  const handleDeleteSheet = async (id: number) => {
    if (!confirm('Are you sure you want to delete this sheet and all its tasks?')) return;
    try {
      await api.deleteTaskSheet(id);
      show('Task sheet deleted successfully', 'success');
      fetchSheets();
    } catch (e: any) {
      show(e.message || 'Error deleting sheet', 'error');
    }
  };

  // Forms State
  const [sheetForm, setSheetForm] = useState({ title: '', description: '', startDate: '', endDate: '', sheetType: 'DAILY' });
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
      setActiveTab('sheets');
    } catch (e: any) {
      show(e.message || 'Error creating sheet', 'error');
    }
  };

  const [taskForm, setTaskForm] = useState({ taskSheetId: '', title: '', description: '', difficulty: 'EASY', topicId: '', platformLink: '', xpReward: 100 });
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
      setActiveTab('sheets');
    } catch (e: any) {
      show(e.message || 'Error creating task', 'error');
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold dark:text-white flex items-center gap-3">
          <Settings className="w-8 h-8 text-blue-500" /> Admin Dashboard
        </h1>
        <p className="text-gray-500 dark:text-gray-400 mt-1">Manage members, task sheets, and content.</p>
      </div>

      {/* Tabs */}
      <div className="flex overflow-x-auto bg-gray-100 dark:bg-slate-800 p-1 rounded-xl">
        {[
          { id: 'members', label: 'Members', icon: Users },
          { id: 'sheets', label: 'Task Sheets', icon: FileText },
          { id: 'create-sheet', label: 'Create Sheet', icon: Plus },
          { id: 'create-task', label: 'Add Task', icon: Plus },
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as any)}
            className={clsx(
              "flex items-center gap-2 px-6 py-2.5 rounded-lg font-medium text-sm transition-all whitespace-nowrap",
              activeTab === tab.id 
                ? "bg-white dark:bg-slate-700 text-blue-600 dark:text-blue-400 shadow-sm" 
                : "text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
            )}
          >
            <tab.icon className="w-4 h-4" /> {tab.label}
          </button>
        ))}
      </div>

      <div className="mt-6">
        {/* MEMBERS TAB */}
        {activeTab === 'members' && (
          <Card className="overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-50 dark:bg-slate-800 border-b border-gray-200 dark:border-slate-700 text-gray-500 dark:text-gray-400 text-sm">
                    <th className="py-3 px-4">Member</th>
                    <th className="py-3 px-4">Email</th>
                    <th className="py-3 px-4">Joined</th>
                    <th className="py-3 px-4">Role</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-slate-700">
                  {members.map(member => (
                    <tr key={member.id} className="hover:bg-gray-50 dark:hover:bg-slate-800/50">
                      <td className="py-3 px-4 flex items-center gap-3">
                        <img src={member.avatarUrl} alt="" className="w-8 h-8 rounded-full" />
                        <span className="font-medium dark:text-white">{member.name}</span>
                      </td>
                      <td className="py-3 px-4 text-sm text-gray-500 dark:text-gray-400">{member.email}</td>
                      <td className="py-3 px-4 text-sm text-gray-500 dark:text-gray-400">
                        {new Date(member.createdAt).toLocaleDateString()}
                      </td>
                      <td className="py-3 px-4">
                        <Badge variant={member.role === 'ROLE_ADMIN' ? 'admin' : 'user'}>
                          {member.role.replace('ROLE_', '')}
                        </Badge>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <select 
                          className="bg-gray-100 dark:bg-slate-700 text-sm rounded-lg px-2 py-1 border-0 focus:ring-2 focus:ring-blue-500 dark:text-white"
                          value={member.role}
                          onChange={(e) => handleRoleChange(member.id, e.target.value)}
                        >
                          <option value="ROLE_USER">User</option>
                          <option value="ROLE_ADMIN">Admin</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        )}

        {/* SHEETS TAB */}
        {activeTab === 'sheets' && (
          <Card className="overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-50 dark:bg-slate-800 border-b border-gray-200 dark:border-slate-700 text-gray-500 dark:text-gray-400 text-sm">
                    <th className="py-3 px-4">Title</th>
                    <th className="py-3 px-4">Type</th>
                    <th className="py-3 px-4">Dates</th>
                    <th className="py-3 px-4 text-center">Tasks</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-slate-700">
                  {sheets.map(sheet => (
                    <tr key={sheet.id} className="hover:bg-gray-50 dark:hover:bg-slate-800/50">
                      <td className="py-3 px-4 font-medium dark:text-white">{sheet.title}</td>
                      <td className="py-3 px-4">
                        <Badge variant="default">{sheet.sheetType}</Badge>
                      </td>
                      <td className="py-3 px-4 text-sm text-gray-500 dark:text-gray-400">
                        {sheet.startDate} to {sheet.endDate}
                      </td>
                      <td className="py-3 px-4 text-center text-sm font-medium dark:text-gray-300">
                        {sheet.tasks?.length || 0}
                      </td>
                      <td className="py-3 px-4 text-right">
                        <button 
                          onClick={() => handleDeleteSheet(sheet.id)}
                          className="text-red-500 hover:text-red-700 text-sm font-medium"
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
            <h2 className="text-xl font-bold dark:text-white mb-6">Create New Task Sheet</h2>
            <form onSubmit={handleCreateSheet} className="space-y-4">
              <div>
                <label className="block text-sm font-medium dark:text-gray-300 mb-1">Title</label>
                <input required type="text" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={sheetForm.title} onChange={e => setSheetForm({...sheetForm, title: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium dark:text-gray-300 mb-1">Description</label>
                <textarea required rows={3} className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={sheetForm.description} onChange={e => setSheetForm({...sheetForm, description: e.target.value})} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300 mb-1">Start Date</label>
                  <input required type="date" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={sheetForm.startDate} onChange={e => setSheetForm({...sheetForm, startDate: e.target.value})} />
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300 mb-1">End Date</label>
                  <input required type="date" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={sheetForm.endDate} onChange={e => setSheetForm({...sheetForm, endDate: e.target.value})} />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium dark:text-gray-300 mb-1">Type</label>
                <select className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={sheetForm.sheetType} onChange={e => setSheetForm({...sheetForm, sheetType: e.target.value})}>
                  <option value="DAILY">DAILY</option>
                  <option value="WEEKLY">WEEKLY</option>
                </select>
              </div>
              <button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2.5 rounded-lg mt-4 transition-colors">
                Create Sheet
              </button>
            </form>
          </Card>
        )}

        {/* CREATE TASK TAB */}
        {activeTab === 'create-task' && (
          <Card className="p-6 max-w-2xl mx-auto">
            <h2 className="text-xl font-bold dark:text-white mb-6">Add Task to Sheet</h2>
            <form onSubmit={handleCreateTask} className="space-y-4">
              <div>
                <label className="block text-sm font-medium dark:text-gray-300 mb-1">Select Task Sheet</label>
                <select required className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={taskForm.taskSheetId} onChange={e => setTaskForm({...taskForm, taskSheetId: e.target.value})}>
                  <option value="">-- Select Sheet --</option>
                  {sheets.map(s => <option key={s.id} value={s.id}>{s.title}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium dark:text-gray-300 mb-1">Task Title</label>
                <input required type="text" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={taskForm.title} onChange={e => setTaskForm({...taskForm, title: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium dark:text-gray-300 mb-1">Description / Hints</label>
                <textarea required rows={2} className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={taskForm.description} onChange={e => setTaskForm({...taskForm, description: e.target.value})} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300 mb-1">Difficulty</label>
                  <select className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={taskForm.difficulty} onChange={e => setTaskForm({...taskForm, difficulty: e.target.value})}>
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300 mb-1">Topic</label>
                  <select required className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={taskForm.topicId} onChange={e => setTaskForm({...taskForm, topicId: e.target.value})}>
                    <option value="">-- Topic --</option>
                    {topics.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-3 gap-4">
                <div className="col-span-2">
                  <label className="block text-sm font-medium dark:text-gray-300 mb-1">Platform Link (LeetCode, etc)</label>
                  <input required type="url" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={taskForm.platformLink} onChange={e => setTaskForm({...taskForm, platformLink: e.target.value})} />
                </div>
                <div>
                  <label className="block text-sm font-medium dark:text-gray-300 mb-1">XP Reward</label>
                  <input required type="number" min="1" className="w-full bg-gray-50 dark:bg-slate-800 border border-gray-300 dark:border-slate-700 rounded-lg p-2.5 dark:text-white" value={taskForm.xpReward} onChange={e => setTaskForm({...taskForm, xpReward: Number(e.target.value)})} />
                </div>
              </div>
              <button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-2.5 rounded-lg mt-4 transition-colors flex items-center justify-center gap-2">
                <Check className="w-5 h-5" /> Add Task
              </button>
            </form>
          </Card>
        )}
      </div>
    </div>
  );
};

export default AdminPage;
