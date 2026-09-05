import { User, DashboardData, TaskSheet, Task, TaskCompletionEntry, LeaderboardEntry, Topic, TopicProgress } from '../types';

const API_BASE = '';

function getCsrfToken(): string | null {
  const match = document.cookie.match(new RegExp('(^|;\\s*)XSRF-TOKEN=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

async function fetchApi<T>(url: string, options?: RequestInit): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options?.headers as Record<string, string>),
  };

  const method = options?.method?.toUpperCase() || 'GET';
  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
    const csrfToken = getCsrfToken();
    if (csrfToken) {
      headers['X-XSRF-TOKEN'] = csrfToken;
    }
  }

  const res = await fetch(`${API_BASE}${url}`, {
    ...options,
    credentials: 'include',
    headers,
  });

  if (!res.ok) {
    throw new Error(`API error: ${res.status}`);
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
  
  // Topics
  getTopics: () => fetchApi<Topic[]>('/api/topics'),
};
