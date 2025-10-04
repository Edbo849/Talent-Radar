import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import Login from "./Login";
import Register from "./Register";
import "../utilities.css";

/**
 * AuthContainer component manages authentication UI.
 * It toggles between Login and Register forms and handles loading state.
 *
 * @param {Function} onAuthSuccess - Callback after successful authentication.
 */
const AuthContainer = ({ onAuthSuccess }) => {
  // State to track whether to show Login or Register form
  const [isLogin, setIsLogin] = useState(true);

  // Get loading state from AuthContext to show spinner while checking auth
  const { loading } = useAuth();

  // Show loading spinner while authentication state is being checked
  if (loading) {
    return (
      <div className="loading-container flex flex-col items-center justify-center bg-navy text-white min-h-full">
        <div className="loading-spinner mb-lg"></div>
        <p className="text-lg">Loading...</p>
      </div>
    );
  }

  // Toggle between Login and Register forms
  const toggleMode = () => {
    setIsLogin(!isLogin);
  };

  // Handle successful login or registration
  const handleSuccess = () => {
    // Call parent callback if provided
    onAuthSuccess && onAuthSuccess();
  };

  // Render either Login or Register component based on isLogin state
  return (
    <>
      {isLogin ? (
        <Login onToggleMode={toggleMode} onSuccess={handleSuccess} />
      ) : (
        <Register onToggleMode={toggleMode} onSuccess={handleSuccess} />
      )}
    </>
  );
};

export default AuthContainer;
