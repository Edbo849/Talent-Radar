import React, { useState, useEffect } from "react";

function App() {
  const [backendStatus, setBackendStatus] = useState("Checking...");

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

  return (
    <div className="App">
      <header className="App-header">
        <h1>🎯 Talent Radar</h1>
        <p>U21 Football Scouting Platform</p>

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

export default App;
