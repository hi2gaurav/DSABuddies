export interface User {
  id: number;
  email: string;
  name: string;
  avatarUrl: string;
  role: string;
  currentStreak: number;
  maxStreak: number;
  totalXp: number;
  createdAt: string;
}

export interface DashboardData {
  userName: string;
  avatarUrl: string;
  currentStreak: number;
  maxStreak: number;
  totalXp: number;
  tasksCompleted: number;
  totalTasks: number;
  completionPercentage: number;
  activeSheet: TaskSheet | null;
  recentCompletions: TaskCompletionEntry[];
}

export interface TaskSheet {
  id: number;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  sheetType: string;
  createdByName: string;
  tasks: Task[];
  createdAt: string;
}

export interface Task {
  id: number;
  title: string;
  description: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  topicName: string;
  topicColor: string;
  platformLink: string;
  xpReward: number;
  completed: boolean;
}

export interface TaskCompletionEntry {
  id: number;
  userId: number;
  userName: string;
  userAvatar: string;
  taskId: number;
  taskTitle: string;
  completedAt: string;
  solutionLink: string;
  notes: string;
}

export interface LeaderboardEntry {
  rank: number;
  userId: number;
  userName: string;
  userAvatar: string;
  totalXp: number;
  currentStreak: number;
  tasksCompleted: number;
}

export interface Topic {
  id: number;
  name: string;
  color: string;
  icon: string;
}

export interface TopicProgress {
  topicName: string;
  topicColor: string;
  completed: number;
  total: number;
  percentage: number;
}
