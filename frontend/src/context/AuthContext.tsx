import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { UserProfile, UserSummary } from '../types';
import { authService, LoginPayload, RegisterPayload, UpdateProfilePayload } from '../services/authService';

interface AuthContextType {
  user: UserSummary | null;
  profile: UserProfile | null;
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  loading: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
  updateProfile: (payload: UpdateProfilePayload) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserSummary | null>(() => {
    const saved = localStorage.getItem('borrowbox_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem('borrowbox_access_token')
  );
  const [loading, setLoading] = useState<boolean>(true);

  const isAdmin = user?.roles?.includes('ROLE_ADMIN') || false;
  const isAuthenticated = !!token && !!user;

  const refreshProfile = async () => {
    if (!token) return;
    try {
      const p = await authService.getMe();
      setProfile(p);
      const summary: UserSummary = {
        id: p.id,
        email: p.email,
        fullName: p.fullName,
        location: p.location,
        profileImageUrl: p.profileImageUrl,
        averageRating: p.averageRating,
        ratingCount: p.ratingCount,
        reputationScore: p.reputationScore,
        roles: p.roles,
        verified: p.verified,
      };
      setUser(summary);
      localStorage.setItem('borrowbox_user', JSON.stringify(summary));
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    const init = async () => {
      if (token) {
        await refreshProfile();
      }
      setLoading(false);
    };
    init();
  }, [token]);

  const login = async (payload: LoginPayload) => {
    const res = await authService.login(payload);
    localStorage.setItem('borrowbox_access_token', res.accessToken);
    localStorage.setItem('borrowbox_refresh_token', res.refreshToken);
    localStorage.setItem('borrowbox_user', JSON.stringify(res.user));
    setToken(res.accessToken);
    setUser(res.user);
    await refreshProfile();
  };

  const register = async (payload: RegisterPayload) => {
    const res = await authService.register(payload);
    localStorage.setItem('borrowbox_access_token', res.accessToken);
    localStorage.setItem('borrowbox_refresh_token', res.refreshToken);
    localStorage.setItem('borrowbox_user', JSON.stringify(res.user));
    setToken(res.accessToken);
    setUser(res.user);
    await refreshProfile();
  };

  const logout = () => {
    localStorage.removeItem('borrowbox_access_token');
    localStorage.removeItem('borrowbox_refresh_token');
    localStorage.removeItem('borrowbox_user');
    setToken(null);
    setUser(null);
    setProfile(null);
  };

  const updateProfile = async (payload: UpdateProfilePayload) => {
    const updated = await authService.updateProfile(payload);
    setProfile(updated);
    await refreshProfile();
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        profile,
        token,
        isAuthenticated,
        isAdmin,
        loading,
        login,
        register,
        logout,
        refreshProfile,
        updateProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
