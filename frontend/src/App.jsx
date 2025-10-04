import React, { useState, useEffect } from "react";
import { AuthProvider, useAuth } from "./context/AuthContext";
import AuthContainer from "./components/AuthContainer";
import "./components/static/Auth.css";
import "./utilities.css";

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

  // If user is authenticated, show the main app
  return (
    <div className="App flex items-center justify-center min-h-full bg-navy text-white">
      <header className="App-header max-w-lg p-3xl">
        <div className="user-info bg-white rounded-xl p-xl mb-xl shadow-lg text-center">
          <h1 className="font-heading text-4xl mb-md text-navy">
            🎯 Welcome to Talent Radar
          </h1>
          <p className="text-lg mb-2xl text-navy">
            Hello, {user.displayName || user.username}!
          </p>
          <button
            onClick={logout}
            className="logout-button bg-navy text-white rounded-md p-md px-xl mt-md font-semibold transition-base w-full"
          >
            Sign Out
          </button>
        </div>

        <div className="status-card bg-white rounded-xl p-xl mb-xl shadow-lg">
          <h3 className="text-xl mb-md text-navy">System Status</h3>
          <p className="text-sm font-mono m-sm text-navy">
            <strong>Frontend:</strong> ✅ Running on port 3000
          </p>
          <p className="text-sm font-mono m-sm text-navy">
            <strong>Backend:</strong> {backendStatus}
          </p>
          <p className="text-sm font-mono m-sm text-navy">
            <strong>API Endpoint:</strong> http://localhost:8080/api
          </p>
          <p className="text-sm font-mono m-sm text-navy">
            <strong>User Role:</strong> {user.role}
          </p>
        </div>

        <div className="quick-links bg-white rounded-xl p-xl mb-xl shadow-lg">
          <h3 className="text-xl mb-md text-navy">Quick Links</h3>
          <ul className="list-none p-0">
            <li className="mb-md">
              <a
                href="http://localhost:8080/api"
                target="_blank"
                rel="noopener noreferrer"
                className="text-navy p-sm px-md rounded-md border border-navy transition-base d-inline-block"
              >
                Backend API
              </a>
            </li>
            <li className="mb-md">
              <a href="#players" className="text-navy">
                Player Search (Coming Soon)
              </a>
            </li>
            <li className="mb-md">
              <a href="#scouting" className="text-navy">
                Scouting Reports (Coming Soon)
              </a>
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
