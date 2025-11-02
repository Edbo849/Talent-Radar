import React, { useState, useEffect, useRef } from "react";
import { useAuth } from "../context/AuthContext";
import { Link, useNavigate } from "react-router-dom";
import ApiService from "../services/api";
import "./static/Navbar.css";

/**
 * Navbar component - top navigation bar for the dashboard
 * Contains logo, search functionality, and user profile menu
 */
const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState("");
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [searchSuggestions, setSearchSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [loadingSuggestions, setLoadingSuggestions] = useState(false);
  const searchRef = useRef(null);

  // Load search suggestions
  useEffect(() => {
    const loadSuggestions = async () => {
      if (searchQuery.trim().length < 2) {
        setSearchSuggestions([]);
        return;
      }

      setLoadingSuggestions(true);
      try {
        const response = await ApiService.searchPlayersSuggestions(searchQuery);
        setSearchSuggestions(response.slice(0, 5)); // Show top 5 results
      } catch (error) {
        console.error("Error loading suggestions:", error);
        setSearchSuggestions([]);
      } finally {
        setLoadingSuggestions(false);
      }
    };

    const debounceTimer = setTimeout(loadSuggestions, 300);
    return () => clearTimeout(debounceTimer);
  }, [searchQuery]);

  // Close suggestions when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  /**
   * Handle search form submission
   */
  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
      setShowSuggestions(false);
    }
  };

  /**
   * Handle search input changes
   */
  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
  };

  /**
   * Handle clicking on a search suggestion
   */
  const handleSuggestionClick = (playerId) => {
    navigate(`/player/${playerId}`);
    setSearchQuery("");
    setShowSuggestions(false);
  };

  /**
   * Toggle profile dropdown menu
   */
  const toggleProfileMenu = () => {
    setIsProfileMenuOpen(!isProfileMenuOpen);
  };

  /**
   * Handle logout
   */
  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <nav className="navbar">
      <div className="navbar-content">
        <Link to="/dashboard" className="navbar-brand">
          <img
            src="/talent_radar.png"
            alt="Talent Radar"
            className="brand-logo"
          />
          <span className="brand-text">TalentRadar</span>
        </Link>

        <div className="navbar-search" ref={searchRef}>
          <form onSubmit={handleSearch} className="search-form">
            <div className="search-input-wrapper">
              <div className="search-icon">
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="M21 21l-4.35-4.35"></path>
                </svg>
              </div>
              <input
                type="text"
                placeholder="Search players, clubs, leagues..."
                value={searchQuery}
                onChange={handleSearchChange}
                onFocus={() => setShowSuggestions(true)}
                className="search-input"
              />
              {searchQuery && (
                <button
                  type="button"
                  onClick={() => {
                    setSearchQuery("");
                    setSearchSuggestions([]);
                  }}
                  className="search-clear"
                >
                  ✕
                </button>
              )}
            </div>

            {/* Search Suggestions Dropdown */}
            {showSuggestions && searchQuery.trim().length >= 2 && (
              <div className="search-suggestions">
                {loadingSuggestions ? (
                  <div className="suggestion-loading">Searching...</div>
                ) : searchSuggestions.length > 0 ? (
                  <>
                    {searchSuggestions.map((player) => (
                      <div
                        key={player.id}
                        className="suggestion-item"
                        onClick={() => handleSuggestionClick(player.id)}
                      >
                        <div className="suggestion-avatar">
                          {player.photoUrl ? (
                            <img src={player.photoUrl} alt={player.name} />
                          ) : (
                            <div className="suggestion-placeholder">
                              {player.name.charAt(0)}
                            </div>
                          )}
                        </div>
                        <div className="suggestion-info">
                          <div className="suggestion-name">{player.name}</div>
                          <div className="suggestion-details">
                            {player.position} • {player.age} years
                            {player.currentClubName &&
                              ` • ${player.currentClubName}`}
                          </div>
                        </div>
                      </div>
                    ))}
                    <div className="suggestion-footer">
                      <button
                        type="submit"
                        className="view-all-results-btn"
                        onClick={handleSearch}
                      >
                        View all results for "{searchQuery}"
                      </button>
                    </div>
                  </>
                ) : (
                  <div className="suggestion-empty">
                    No players found. Press Enter to search.
                  </div>
                )}
              </div>
            )}
          </form>
        </div>

        <div className="navbar-actions">
          <div className="user-menu">
            <button className="user-button" onClick={toggleProfileMenu}>
              <div className="user-avatar">
                {user.displayName?.charAt(0) || user.username?.charAt(0)}
              </div>
              <span className="user-name">
                {user.displayName || user.username}
              </span>
              <svg
                className={`dropdown-arrow ${isProfileMenuOpen ? "open" : ""}`}
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <polyline points="6 9 12 15 18 9"></polyline>
              </svg>
            </button>

            {isProfileMenuOpen && (
              <div className="dropdown-menu">
                <Link to="/profile" className="dropdown-item">
                  <span className="dropdown-icon">👤</span>
                  Profile
                </Link>
                <Link to="/settings" className="dropdown-item">
                  <span className="dropdown-icon">⚙️</span>
                  Settings
                </Link>
                <div className="dropdown-divider"></div>
                <button onClick={handleLogout} className="dropdown-item logout">
                  <span className="dropdown-icon">🚪</span>
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
