import React, { useState, useEffect } from "react";
import { AuthProvider, useAuth } from "./context/AuthContext";
import AuthContainer from "./components/AuthContainer";
import Dashboard from "./components/Dashboard";
import "./App.css";
import "./components/static/Auth.css";
import "./utilities.css";
function AppContent() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="loading-container flex flex-col items-center justify-center bg-navy text-white min-h-full">
        <div className="loading-spinner mb-lg"></div>
        <p className="text-lg">Loading...</p>
      </div>
    );
  }

  // If user is not authenticated, show auth forms
  if (!user) {
    return <AuthContainer />;
  }

  // If user is authenticated, show dashboard
  return <Dashboard />;
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
