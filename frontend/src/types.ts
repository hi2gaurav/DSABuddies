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
  level?: number;
  title?: string;
  dailyGoal?: number;
  consistencyScore?: number;
  streakFreezeAvailable?: boolean;
  streakFreezeUsedDate?: string | null;
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
  companyTags?: string;
  patternTags?: string;
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
  newBadges?: Badge[];
  levelUp?: boolean;
  newLevel?: number;
  newTitle?: string;
}

export interface LeaderboardEntry {
  rank: number;
  userId: number;
  userName: string;
  userAvatar: string;
  totalXp: number;
  currentStreak: number;
  tasksCompleted: number;
  level?: number;
  title?: string;
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

export interface LeetCodeProblem {
  id: string;
  title: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  topic: string;
  url: string;
  problemSummary: string;
  optimalApproach: string;
  timeComplexity: string;
  spaceComplexity: string;
}

export interface DesignTopic {
  id: string;
  title: string;
  type: 'LLD' | 'HLD';
  difficulty: string;
  description: string;
  coreRequirements: string[];
  designPatternsOrComponents: string[];
  architectureSummary: string;
}

export interface InterviewQuestion {
  id: number;
  category: string;
  topic: string;
  question: string;
  answer: string;
  keyPoints?: string[];
  codeSnippet?: string;
}

export interface DailyContent {
  date: string;
  dayOfYear: number;
  quoteOfTheDay: string;
  leetCodeProblem: LeetCodeProblem;
  lldTopic: DesignTopic;
  hldTopic: DesignTopic;
  javaQuestions: InterviewQuestion[];
  springBootQuestions: InterviewQuestion[];
  databaseQuestions: InterviewQuestion[];
  csSubjectsQuestions: InterviewQuestion[];
}

export interface ReviewItem {
  id: number;
  taskId: number;
  taskTitle: string;
  taskDescription: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  topicName: string;
  topicColor: string;
  platformLink: string;
  xpReward: number;
  nextReviewDate: string;
  intervalDays: number;
  easeFactor: number;
  reviewCount: number;
  lastReviewedAt: string;
}

export interface Bookmark {
  id: number;
  taskId: number;
  taskTitle: string;
  taskDescription: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  topicName: string;
  topicColor: string;
  platformLink: string;
  xpReward: number;
  createdAt: string;
}

export interface UserNote {
  id: number;
  taskId: number;
  taskTitle: string;
  content: string;
  codeSnippet: string;
  language: string;
  updatedAt: string;
}

export interface SaveNoteRequest {
  content?: string;
  codeSnippet?: string;
  language?: string;
}

export interface WeakTopic {
  topicId: number;
  topicName: string;
  topicColor: string;
  totalProblems: number;
  solvedProblems: number;
  completionPercentage: number;
  averageRating: number | null;
  recommendation: string;
}

export interface AdaptiveSuggestion {
  taskId: number;
  title: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  topicName: string;
  topicColor: string;
  platformLink: string;
  xpReward: number;
  reason: string;
}

export interface PatternStat {
  pattern: string;
  totalCount: number;
  solvedCount: number;
  masteryPercentage: number;
}

export interface Badge {
  id: number;
  name: string;
  description: string;
  icon: string;
  category: string;
  criteriaType: string;
  criteriaValue: number;
  xpReward: number;
  rarity: 'COMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
  earned: boolean;
  earnedAt?: string;
  progressPercent: number;
}

export interface LeaderboardSnapshot {
  id: number;
  periodType: 'WEEKLY' | 'MONTHLY';
  periodStart: string;
  periodEnd: string;
  snapshotData: string;
  createdAt: string;
}



