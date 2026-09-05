import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './hooks/useAuth';
import { ThemeProvider } from './hooks/useTheme';
import { ToastProvider } from './components/ui/Toast';
import DashboardLayout from './components/layout/DashboardLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import TasksPage from './pages/TasksPage';
import TaskSheetDetailPage from './pages/TaskSheetDetailPage';
import LeaderboardPage from './pages/LeaderboardPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import BookmarksPage from './pages/BookmarksPage';
import MockInterviewPage from './pages/MockInterviewPage';
import LoadingSpinner from './components/ui/LoadingSpinner';

const ProtectedRoute = ({ children, adminOnly = false }: { children: React.ReactNode, adminOnly?: boolean }) => {
  const { authenticated, loading, user } = useAuth();
  
  if (loading) {
    return <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-slate-900"><LoadingSpinner /></div>;
  }
  
  if (!authenticated) {
    return <Navigate to="/" replace />;
  }

  if (adminOnly && user?.role !== 'ROLE_ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }
  
  return <>{children}</>;
};

const AppRoutes = () => {
  const { authenticated, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-slate-900"><LoadingSpinner /></div>;
  }

  return (
    <Routes>
      <Route path="/" element={authenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />} />
      
      <Route element={<ProtectedRoute><DashboardLayout /></ProtectedRoute>}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/tasks" element={<TasksPage />} />
        <Route path="/tasks/:id" element={<TaskSheetDetailPage />} />
        <Route path="/mock-interview" element={<MockInterviewPage />} />
        <Route path="/designs" element={<Navigate to="/dashboard" replace />} />
        <Route path="/flashcards" element={<Navigate to="/dashboard" replace />} />
        <Route path="/reviews" element={<Navigate to="/dashboard" replace />} />
        <Route path="/bookmarks" element={<BookmarksPage />} />
        <Route path="/leaderboard" element={<LeaderboardPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/profile/:id" element={<ProfilePage />} />
        
        <Route path="/admin" element={
          <ProtectedRoute adminOnly={true}>
            <AdminPage />
          </ProtectedRoute>
        } />
      </Route>
    </Routes>
  );
};

const App = () => {
  return (
    <ThemeProvider>
      <ToastProvider>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </ToastProvider>
    </ThemeProvider>
  );
};

export default App;
