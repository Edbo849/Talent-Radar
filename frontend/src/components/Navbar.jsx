import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import "./static/Navbar.css";

/**
 * Navbar component - top navigation bar for the dashboard
 * Contains logo, search functionality, and user profile menu
 */
const Navbar = () => {
  const { user, logout } = useAuth();
  const [searchQuery, setSearchQuery] = useState("");
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);

  /**
   * Handle search form submission
   */
  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      // TODO: Implement search functionality
      console.log("Searching for:", searchQuery);
    }
  };

  /**
   * Handle search input changes
   */
  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
  };

  /**
   * Toggle profile dropdown menu
   */
  const toggleProfileMenu = () => {
    setIsProfileMenuOpen(!isProfileMenuOpen);
  };

  /**
   * Close profile menu when clicking outside
   */
  const closeProfileMenu = () => {
    setIsProfileMenuOpen(false);
  };

  /**
   * Handle logout
   */
  const handleLogout = () => {
    logout();
    closeProfileMenu();
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        {/* Left: Logo and Brand */}
        <div className="navbar-left">
          <div className="navbar-brand">
            <img
              src="/talent_radar.png"
              alt="Talent Radar"
              className="navbar-logo"
            />
            <div className="brand-text">
              <h1 className="brand-title">Talent Radar</h1>
              <p className="brand-subtitle">Scout. Discover. Excel.</p>
            </div>
          </div>
        </div>

        {/* Center: Search Bar */}
        <div className="navbar-center">
          <form onSubmit={handleSearch} className="search-form">
            <div className="search-container">
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
                className="search-input"
              />
              {searchQuery && (
                <button
                  type="button"
                  onClick={() => setSearchQuery("")}
                  className="search-clear"
                >
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              )}
            </div>
          </form>
        </div>

        {/* Right: Profile Menu */}
        <div className="navbar-right">
          <div className="profile-section">
            {/* Notifications (placeholder) */}
            <button className="notification-btn">
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
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
              </svg>
              <span className="notification-badge">3</span>
            </button>

            {/* Profile Dropdown */}
            <div className="profile-dropdown">
              <button
                onClick={toggleProfileMenu}
                className="profile-trigger"
                aria-expanded={isProfileMenuOpen}
              >
                <div className="profile-avatar">
                  {user.profileImageUrl ? (
                    <img
                      src={user.profileImageUrl}
                      alt={user.displayName || user.username}
                      className="avatar-image"
                    />
                  ) : (
                    <div className="avatar-placeholder">
                      {(user.displayName || user.username)
                        .charAt(0)
                        .toUpperCase()}
                    </div>
                  )}
                </div>
                <div className="profile-info">
                  <span className="profile-name">
                    {user.displayName || user.username}
                  </span>
                  <span className="profile-role">{user.role}</span>
                </div>
                <div className="dropdown-arrow">
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className={isProfileMenuOpen ? "rotated" : ""}
                  >
                    <polyline points="6,9 12,15 18,9"></polyline>
                  </svg>
                </div>
              </button>

              {/* Dropdown Menu */}
              {isProfileMenuOpen && (
                <>
                  <div
                    className="dropdown-overlay"
                    onClick={closeProfileMenu}
                  ></div>
                  <div className="dropdown-menu">
                    <div className="dropdown-header">
                      <div className="dropdown-user-info">
                        <p className="dropdown-user-name">
                          {user.displayName || user.username}
                        </p>
                        <p className="dropdown-user-email">{user.email}</p>
                      </div>
                    </div>
                    <div className="dropdown-divider"></div>
                    <div className="dropdown-items">
                      <button className="dropdown-item">
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                        >
                          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                          <circle cx="12" cy="7" r="4"></circle>
                        </svg>
                        <span>Profile</span>
                      </button>
                      <button className="dropdown-item">
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                        >
                          <circle cx="12" cy="12" r="3"></circle>
                          <path d="M12 1v6m0 6v6m11-7h-6m-6 0H1"></path>
                        </svg>
                        <span>Settings</span>
                      </button>
                      <button className="dropdown-item">
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                        >
                          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                          <polyline points="16,17 21,12 16,7"></polyline>
                          <line x1="21" y1="12" x2="9" y2="12"></line>
                        </svg>
                        <span>Help & Support</span>
                      </button>
                    </div>
                    <div className="dropdown-divider"></div>
                    <div className="dropdown-items">
                      <button
                        className="dropdown-item logout-item"
                        onClick={handleLogout}
                      >
                        <svg
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                        >
                          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                          <polyline points="16,17 21,12 16,7"></polyline>
                          <line x1="21" y1="12" x2="9" y2="12"></line>
                        </svg>
                        <span>Sign Out</span>
                      </button>
                    </div>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
