import React, { createContext, useContext, useEffect, useState } from 'react';
import { User } from '../types';
import { api } from '../lib/api';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  authenticated: boolean;
  login: () => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const { authenticated } = await api.getAuthStatus();
        if (authenticated) {
          setAuthenticated(true);
          try {
            const currentUser = await api.getCurrentUser();
            setUser(currentUser);
          } catch (e) {
            console.error('Failed to fetch user details:', e);
          }
        } else {
          setAuthenticated(false);
          setUser(null);
        }
      } catch (error) {
        console.error('Failed to check auth status:', error);
        setAuthenticated(false);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    checkAuth();
  }, []);

  const login = () => {
    window.location.href = '/oauth2/authorization/google';
  };

  const logout = async () => {
    window.location.href = '/logout';
  };

  return (
    <AuthContext.Provider value={{ user, loading, authenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
