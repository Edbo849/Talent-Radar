import React, { createContext, useState, useContext, useEffect } from "react";
import ApiService from "../services/api";

// Create the authentication context
const AuthContext = createContext();

// Custom hook for accessing the AuthContext
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

// AuthProvider component to wrap parts of the app that need authentication
export const AuthProvider = ({ children }) => {
  // State to store user info, token, and loading status
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() => localStorage.getItem("token")); // Use lazy initializer for performance
  const [loading, setLoading] = useState(true);

  // Effect to fetch current user if token exists
  useEffect(() => {
    if (token) {
      ApiService.getCurrentUser(token)
        .then(setUser)
        .catch(() => {
          localStorage.removeItem("token");
          setToken(null);
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [token]);

  // Login function
  const login = async (credentials) => {
    const response = await ApiService.login(credentials);
    localStorage.setItem("token", response.token);
    setToken(response.token);
    setUser(response.user);
    return response;
  };

  // Register function
  const register = async (userData) => {
    const response = await ApiService.register(userData);
    localStorage.setItem("token", response.token);
    setToken(response.token);
    setUser(response.user);
    return response;
  };

  // Logout function
  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
    setUser(null);
  };

  // Context value to be provided to consumers
  const value = {
    user, // Current user object or null
    token, // JWT token or null
    login, // Function to log in
    register, // Function to register
    logout, // Function to log out
    loading, // Loading state for auth checks
    isAuthenticated: Boolean(user), // True if user is logged in
  };

  // Provide the context value to children components
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
