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
  const [user, setUser] = useState<User | null>(() => {
    try {
      const cached = localStorage.getItem('dsabuddies_auth_user');
      return cached ? JSON.parse(cached) : null;
    } catch {
      return null;
    }
  });
  const [authenticated, setAuthenticated] = useState<boolean>(() => {
    try {
      return !!localStorage.getItem('dsabuddies_auth_user');
    } catch {
      return false;
    }
  });
  const [loading, setLoading] = useState(!user);

  useEffect(() => {
    let isMounted = true;

    const checkAuth = async (isBackgroundCheck = false) => {
      if (!isBackgroundCheck && !user) {
        setLoading(true);
      }
      try {
        const { authenticated: serverAuth } = await api.getAuthStatus();
        if (!isMounted) return;

        if (serverAuth) {
          setAuthenticated(true);
          try {
            const currentUser = await api.getCurrentUser();
            if (isMounted) {
              setUser(currentUser);
              try {
                localStorage.setItem('dsabuddies_auth_user', JSON.stringify(currentUser));
              } catch {}
            }
          } catch (e) {
            console.error('Failed to fetch user details:', e);
          }
        } else {
          // Explicitly unauthenticated by server
          setAuthenticated(false);
          setUser(null);
          try {
            localStorage.removeItem('dsabuddies_auth_user');
          } catch {}
        }
      } catch (error) {
        console.warn('Transient network error while checking auth status, keeping cached session:', error);
        // Do NOT log out user on network drops or tab switches
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };
    
    checkAuth(false);

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        checkAuth(true);
      }
    };
    
    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      isMounted = false;
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  const login = () => {
    window.location.href = '/oauth2/authorization/google';
  };

  const logout = async () => {
    try {
      localStorage.removeItem('dsabuddies_auth_user');
    } catch {}
    setUser(null);
    setAuthenticated(false);
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
