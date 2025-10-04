import React, { useState, useEffect } from "react";
import { AuthProvider, useAuth } from "./context/AuthContext";
import AuthContainer from "./components/AuthContainer";
import "./components/static/Auth.css";

function AppContent() {
  const [backendStatus, setBackendStatus] = useState("Checking...");
  const { user, logout, loading } = useAuth();

  useEffect(() => {
    // Test backend connection
    fetch("http://localhost:8080/api")
      .then((response) => {
        if (response.ok) {
          setBackendStatus("✅ Connected");
        } else {
          setBackendStatus("⚠️ Backend running but not accessible");
        }
      })
      .catch((error) => {
        setBackendStatus("❌ Backend not reachable");
      });
  }, []);

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  // If user is not authenticated, show auth forms
  if (!user) {
    return <AuthContainer />;
  }

  // If user is authenticated, show the main app
  return (
    <div className="App">
      <header className="App-header">
        <div className="user-info">
          <h1>🎯 Welcome to Talent Radar</h1>
          <p>Hello, {user.displayName || user.username}!</p>
          <button onClick={logout} className="logout-button">
            Sign Out
          </button>
        </div>

        <div className="status-card">
          <h3>System Status</h3>
          <p>
            <strong>Frontend:</strong> ✅ Running on port 3000
          </p>
          <p>
            <strong>Backend:</strong> {backendStatus}
          </p>
          <p>
            <strong>API Endpoint:</strong> http://localhost:8080/api
          </p>
          <p>
            <strong>User Role:</strong> {user.role}
          </p>
        </div>

        <div className="quick-links">
          <h3>Quick Links</h3>
          <ul>
            <li>
              <a
                href="http://localhost:8080/api"
                target="_blank"
                rel="noopener noreferrer"
              >
                Backend API
              </a>
            </li>
            <li>
              <a href="#players">Player Search (Coming Soon)</a>
            </li>
            <li>
              <a href="#scouting">Scouting Reports (Coming Soon)</a>
            </li>
          </ul>
        </div>
      </header>
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
