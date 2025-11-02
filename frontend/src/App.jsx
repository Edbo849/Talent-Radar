import React, { useState, useEffect } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import { AuthProvider, useAuth } from "./context/AuthContext";
import AuthContainer from "./components/AuthContainer";
import Dashboard from "./components/Dashboard";
import Player from "./components/Player";
import SearchPage from "./components/Search";
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

  // If user is authenticated, show routed content
  return (
    <Routes>
      <Route path="/" element={<Dashboard />} />
      <Route path="/player/:playerId" element={<Player />} />
      <Route path="*" element={<Navigate to="/" replace />} />
      <Route path="/search" element={<SearchPage />} />
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
}

export default App;
