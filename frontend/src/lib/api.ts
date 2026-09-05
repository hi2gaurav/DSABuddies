import { User, DashboardData, TaskSheet, Task, TaskCompletionEntry, LeaderboardEntry, Topic, TopicProgress, DailyContent } from '../types';

const API_BASE = '';

async function fetchApi<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${url}`, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!res.ok) {
    let errorMsg = `API error: ${res.status}`;
    try {
      const errData = await res.json();
      if (errData.details && typeof errData.details === 'object') {
        errorMsg = Object.values(errData.details).join(', ');
      } else if (errData.error) {
        errorMsg = errData.error;
      } else if (errData.message) {
        errorMsg = errData.message;
      }
    } catch (_) {
      // ignore JSON parse error for error body
    }
    throw new Error(errorMsg);
  }

  const contentType = res.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return res.json();
  }
  return {} as T;
}

export const api = {
  // Auth
  getAuthStatus: () => fetchApi<{authenticated: boolean}>('/api/auth/status'),
  getCurrentUser: () => fetchApi<User>('/api/auth/me'),
  
  // Dashboard
  getDashboard: () => fetchApi<DashboardData>('/api/dashboard'),
  
  // Task Sheets
  getTaskSheets: (active?: boolean) => fetchApi<TaskSheet[]>(`/api/task-sheets${active ? '?active=true' : ''}`),
  getTaskSheet: (id: number) => fetchApi<TaskSheet>(`/api/task-sheets/${id}`),
  createTaskSheet: (data: any) => fetchApi<TaskSheet>('/api/task-sheets', { method: 'POST', body: JSON.stringify(data) }),
  deleteTaskSheet: (id: number) => fetchApi<void>(`/api/task-sheets/${id}`, { method: 'DELETE' }),
  
  // Tasks
  createTask: (data: any) => fetchApi<Task>('/api/tasks', { method: 'POST', body: JSON.stringify(data) }),
  deleteTask: (id: number) => fetchApi<void>(`/api/tasks/${id}`, { method: 'DELETE' }),
  
  // Completions
  completeTask: (taskId: number, data?: any) => fetchApi<TaskCompletionEntry>(`/api/tasks/${taskId}/complete`, { method: 'POST', body: JSON.stringify(data || {}) }),
  uncompleteTask: (taskId: number) => fetchApi<void>(`/api/tasks/${taskId}/complete`, { method: 'DELETE' }),
  
  // Leaderboard
  getLeaderboard: (period?: string) => fetchApi<LeaderboardEntry[]>(`/api/leaderboard${period ? `?period=${period}` : ''}`),
  
  // Profile
  getProfile: (id?: number) => fetchApi<User>(id ? `/api/profile/${id}` : '/api/profile/me'),
  getTopicProgress: (id: number) => fetchApi<TopicProgress[]>(`/api/profile/${id}/topics`),
  getActivity: (id: number) => fetchApi<Record<string, number>>(`/api/profile/${id}/activity`),
  
  // Admin
  getMembers: () => fetchApi<User[]>('/api/admin/members'),
  updateMemberRole: (id: number, role: string) => fetchApi<void>(`/api/admin/members/${id}/role`, { method: 'PUT', body: JSON.stringify({ role }) }),
  getDailyContent: () => fetchApi<DailyContent>('/api/admin/daily-content'),
  refreshDailyContent: () => fetchApi<DailyContent>('/api/admin/daily-content/refresh', { method: 'POST' }),

  // AI Jeetu Bhaiya Assistant
  chatWithJeetuBhaiya: (messages: { role: string; content: string }[]) =>
    fetchApi<{ reply: string }>('/api/ai/jeetu-bhaiya', {
      method: 'POST',
      body: JSON.stringify({ messages }),
    }),
  
  // Topics
  getTopics: () => fetchApi<Topic[]>('/api/topics'),

  // Spaced Repetition
  getDueReviews: () => fetchApi<import('../types').ReviewItem[]>('/api/reviews/due'),
  getUpcomingReviews: () => fetchApi<import('../types').ReviewItem[]>('/api/reviews/upcoming'),
  getDueReviewCount: () => fetchApi<{ dueCount: number }>('/api/reviews/count'),
  submitReview: (taskId: number, rating: number) => fetchApi<import('../types').ReviewItem>(`/api/reviews/${taskId}/submit`, {
    method: 'POST',
    body: JSON.stringify({ rating }),
  }),

  // Bookmarks
  getBookmarks: () => fetchApi<import('../types').Bookmark[]>('/api/bookmarks'),
  addBookmark: (taskId: number) => fetchApi<import('../types').Bookmark>(`/api/bookmarks/${taskId}`, { method: 'POST' }),
  removeBookmark: (taskId: number) => fetchApi<void>(`/api/bookmarks/${taskId}`, { method: 'DELETE' }),
  checkBookmark: (taskId: number) => fetchApi<{ bookmarked: boolean }>(`/api/bookmarks/${taskId}/status`),

  // Personal Notes
  getNotes: () => fetchApi<import('../types').UserNote[]>('/api/notes'),
  getNote: (taskId: number) => fetchApi<import('../types').UserNote>(`/api/notes/${taskId}`),
  saveNote: (taskId: number, data: import('../types').SaveNoteRequest) => fetchApi<import('../types').UserNote>(`/api/notes/${taskId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),
  deleteNote: (taskId: number) => fetchApi<void>(`/api/notes/${taskId}`, { method: 'DELETE' }),

  // Analytics & Weak Topics
  getWeakTopics: () => fetchApi<import('../types').WeakTopic[]>('/api/analytics/weak-topics'),
  getAdaptiveSuggestions: () => fetchApi<import('../types').AdaptiveSuggestion[]>('/api/analytics/suggestions'),
  getPatternStats: () => fetchApi<import('../types').PatternStat[]>('/api/analytics/patterns'),

  // Badges & Achievements
  getBadges: () => fetchApi<import('../types').Badge[]>('/api/badges'),
  getMyBadges: () => fetchApi<import('../types').Badge[]>('/api/badges/mine'),
  getUserBadges: (userId: number) => fetchApi<import('../types').Badge[]>(`/api/badges/user/${userId}`),
  updateSettings: (data: { dailyGoal?: number; useStreakFreeze?: boolean }) => fetchApi<User>('/api/profile/settings', {
    method: 'PUT',
    body: JSON.stringify(data),
  }),

  // Leaderboard History
  getLeaderboardHistory: (period?: string) => fetchApi<import('../types').LeaderboardSnapshot[]>(`/api/leaderboard/history?period=${period || 'weekly'}`),

  // Mock Interview
  startMockSession: (data?: import('../types').StartMockRequest) => fetchApi<import('../types').MockSession>('/api/mock/start', {
    method: 'POST',
    body: JSON.stringify(data || {}),
  }),
  submitMockAnswer: (sessionId: number, questionId: number, data: import('../types').SubmitMockAnswerRequest) => fetchApi<import('../types').MockSession>(`/api/mock/${sessionId}/answer/${questionId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),
  completeMockSession: (sessionId: number) => fetchApi<import('../types').MockSession>(`/api/mock/${sessionId}/complete`, {
    method: 'PUT',
  }),
  abandonMockSession: (sessionId: number) => fetchApi<import('../types').MockSession>(`/api/mock/${sessionId}/abandon`, {
    method: 'PUT',
  }),
  getMockSession: (sessionId: number) => fetchApi<import('../types').MockSession>(`/api/mock/${sessionId}`),
  getMockHistory: () => fetchApi<import('../types').MockSession[]>('/api/mock/history'),

  // System Design Templates & User Designs
  getDesignTemplates: (category?: string) => fetchApi<import('../types').DesignTemplate[]>(`/api/designs/templates${category ? `?category=${category}` : ''}`),
  getDesignTemplate: (id: number) => fetchApi<import('../types').DesignTemplate>(`/api/designs/templates/${id}`),
  getMyDesigns: () => fetchApi<import('../types').UserDesign[]>('/api/designs/mine'),
  saveDesign: (data: import('../types').SaveDesignRequest) => fetchApi<import('../types').UserDesign>('/api/designs', {
    method: 'POST',
    body: JSON.stringify(data),
  }),
  updateDesign: (id: number, data: import('../types').SaveDesignRequest) => fetchApi<import('../types').UserDesign>(`/api/designs/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),
  deleteDesign: (id: number) => fetchApi<void>(`/api/designs/${id}`, { method: 'DELETE' }),

  // Flashcards (SuperMemo SM-2)
  getFlashcards: (category?: string) => fetchApi<import('../types').Flashcard[]>(`/api/flashcards${category ? `?category=${category}` : ''}`),
  getDueFlashcards: () => fetchApi<import('../types').Flashcard[]>('/api/flashcards/due'),
  submitFlashcardReview: (id: number, rating: number) => fetchApi<import('../types').Flashcard>(`/api/flashcards/${id}/review`, {
    method: 'POST',
    body: JSON.stringify({ rating }),
  }),

  // Admin Analytics & Moderation (Phase 5)
  getAdminOverview: () => fetchApi<import('../types').AdminOverviewStats>('/api/admin/analytics/overview'),
  getAdminEngagement: (days?: number) => fetchApi<import('../types').EngagementTrend[]>(`/api/admin/analytics/engagement?days=${days || 14}`),
  getAdminTopicDropoff: () => fetchApi<import('../types').TopicDropOff[]>('/api/admin/analytics/topic-dropoff'),
  getAdminSheetAnalytics: (sheetId: number) => fetchApi<import('../types').SheetAnalytics>(`/api/admin/analytics/sheet-stats/${sheetId}`),
  getAdminAuditLogs: () => fetchApi<import('../types').AuditLog[]>('/api/admin/analytics/audit-logs'),
  updateMemberStatus: (userId: number, data: import('../types').UpdateMemberStatusRequest) => fetchApi<User>(`/api/admin/members/${userId}/status`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),

  // Announcements & Broadcasts
  broadcastAnnouncement: (data: import('../types').BroadcastRequest) => fetchApi<import('../types').Announcement>('/api/admin/broadcast', {
    method: 'POST',
    body: JSON.stringify(data),
  }),
  getAdminAnnouncements: () => fetchApi<import('../types').Announcement[]>('/api/admin/announcements'),
  deleteAnnouncement: (id: number) => fetchApi<void>(`/api/admin/announcements/${id}`, { method: 'DELETE' }),
  getActiveAnnouncements: () => fetchApi<import('../types').Announcement[]>('/api/announcements'),
};

