import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import Navbar from "./Navbar";
import ApiService from "../services/api";
import FlagService from "../services/flagService";
import "./static/Dashboard.css";

const CountryFlag = ({ nationality, className = "nationality-flag" }) => {
  const [flag, setFlag] = useState(null);

  useEffect(() => {
    if (nationality) {
      const flagData = FlagService.getFlag(nationality);
      setFlag(flagData);
    }
  }, [nationality]);

  if (!flag) return null;

  if (flag.type === "url") {
    return (
      <img
        src={flag.value}
        alt={`${nationality} flag`}
        className={`${className} flag-image`}
        onError={(e) => {
          // Fallback to emoji if image fails to load
          const emoji = FlagService.getEmojiFlag(nationality);
          e.target.style.display = "none";
          e.target.nextSibling.style.display = "inline";
          e.target.nextSibling.textContent = emoji;
        }}
      />
    );
  }

  return <span className={className}>{flag.value}</span>;
};

/**
 * Dashboard component - main application interface after login
 * Contains role-based content for different user types
 */
const Dashboard = () => {
  const { user, token } = useAuth();
  const [dashboardData, setDashboardData] = useState({
    trendingPlayers: [],
    topRatedPlayers: [],
    latestReports: [],
    activeDiscussions: [],
    activePolls: [],
    myActivity: null,
    scoutData: null,
    adminData: null,
    platformStats: null,
    topRatedPlayersSeason: [],
    showMorePlayers: false,
    loading: true,
  });

  useEffect(() => {
    FlagService.loadCountries();
    loadDashboardData();
  }, [user, token]);

  // function to load more players
  const loadMorePlayers = async () => {
    try {
      const morePlayers = await ApiService.getTopRatedPlayersSeason(50);
      setDashboardData((prev) => ({
        ...prev,
        topRatedPlayersSeason: morePlayers,
        showMorePlayers: true,
      }));
    } catch (error) {
      console.error("Error loading more players:", error);
    }
  };

  const loadDashboardData = async () => {
    try {
      setDashboardData((prev) => ({ ...prev, loading: true }));

      // Load common data for all users
      const commonPromises = [
        ApiService.getTrendingPlayers(5),
        ApiService.getTopRatedPlayers(5),
        ApiService.getLatestReports(5),
        ApiService.getMostActiveDiscussions(5),
        ApiService.getActivePolls(3),
        ApiService.getMyActivity(token),
        ApiService.getTopRatedPlayersSeason(50),
      ];

      const [
        trendingPlayers,
        topRatedPlayers,
        latestReports,
        activeDiscussions,
        activePolls,
        myActivity,
        topRatedPlayersSeason,
      ] = await Promise.all(commonPromises);

      const newData = {
        trendingPlayers,
        topRatedPlayers,
        latestReports,
        activeDiscussions,
        activePolls,
        myActivity,
        topRatedPlayersSeason,
        loading: false,
      };

      // Load role-specific data
      if (user.role === "SCOUT" || user.role === "COACH") {
        const [scoutStats, recentPlayers, topGoalScorers] = await Promise.all([
          ApiService.getScoutStats(token),
          ApiService.getRecentPlayers(10),
          ApiService.getTopPlayersByStats("goals", 2024, 5),
        ]);

        newData.scoutData = {
          ...scoutStats,
          recentPlayers,
          topGoalScorers,
        };
      }

      if (user.role === "ADMIN") {
        const [adminData, platformStats] = await Promise.all([
          ApiService.getAdminStats(token),
          ApiService.getPlatformStats(token),
        ]);
        newData.adminData = adminData;
        newData.platformStats = platformStats;
      }

      setDashboardData(newData);
    } catch (error) {
      console.error("Error loading dashboard data:", error);
      setDashboardData((prev) => ({ ...prev, loading: false }));
    }
  };

  if (dashboardData.loading) {
    return (
      <div className="dashboard-container">
        <Navbar />
        <div className="dashboard-content">
          <div className="loading-state">
            <div className="loading-spinner"></div>
            <p>Loading your dashboard...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <Navbar />
      <div className="dashboard-content">
        {/* Welcome Section */}
        <div className="welcome-section">
          <div className="welcome-header">
            <h1 className="welcome-title">
              🎯 Welcome back, {user.displayName || user.username}!
            </h1>
            <p className="welcome-subtitle">
              {user.role === "ADMIN"
                ? "Manage and monitor your platform"
                : user.role === "SCOUT" || user.role === "COACH"
                ? "Discover and analyse talent"
                : "Explore football talent and insights"}
            </p>
          </div>
        </div>

        {/* Role-based Dashboard Content */}
        {user.role === "ADMIN" ? (
          <AdminDashboard
            adminData={dashboardData.adminData}
            platformStats={dashboardData.platformStats}
          />
        ) : user.role === "SCOUT" || user.role === "COACH" ? (
          <ScoutDashboard
            scoutData={dashboardData.scoutData}
            trendingPlayers={dashboardData.trendingPlayers}
            myActivity={dashboardData.myActivity}
          />
        ) : (
          <UserDashboard
            trendingPlayers={dashboardData.trendingPlayers}
            topRatedPlayers={dashboardData.topRatedPlayers}
            latestReports={dashboardData.latestReports}
            activeDiscussions={dashboardData.activeDiscussions}
            activePolls={dashboardData.activePolls}
            myActivity={dashboardData.myActivity}
            topRatedPlayersSeason={dashboardData.topRatedPlayersSeason}
          />
        )}
      </div>
    </div>
  );
};

// Enhanced General User Dashboard
const UserDashboard = ({
  trendingPlayers,
  topRatedPlayers,
  latestReports,
  activeDiscussions,
  activePolls,
  myActivity,
  topRatedPlayersSeason,
}) => {
  const [showMorePlayers, setShowMorePlayers] = useState(false);
  const [allPlayers, setAllPlayers] = useState(topRatedPlayersSeason);

  const loadMorePlayers = async () => {
    try {
      const morePlayers = await ApiService.getTopRatedPlayersSeason(50);
      setAllPlayers(morePlayers);
      setShowMorePlayers(true);
    } catch (error) {
      console.error("Error loading more players:", error);
    }
  };

  const displayedPlayers = showMorePlayers
    ? allPlayers
    : topRatedPlayersSeason.slice(0, 10);

  return (
    <div className="dashboard-grid">
      {/* Trending Players */}
      <div className="dashboard-section col-span-4">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">
              <div className="info-tooltip">
                <span className="info-icon">ⓘ</span>
                <div className="tooltip-content">
                  Most viewed players over the last week
                </div>
              </div>
              🔥 Trending Players
            </h3>
            <button className="view-all-btn">View All</button>
          </div>
          <div className="card-content">
            <div className="player-list">
              {trendingPlayers.map((player) => (
                <div key={player.id} className="trending-player-item">
                  <div className="player-avatar">
                    {player.photoUrl ? (
                      <img src={player.photoUrl} alt={player.name} />
                    ) : (
                      <div className="avatar-placeholder">
                        {player.name.charAt(0)}
                      </div>
                    )}
                  </div>
                  <div className="player-info">
                    <h4 className="player-name">{player.name}</h4>
                    <div className="player-nationality">
                      {player.nationality && (
                        <>
                          <CountryFlag nationality={player.nationality} />
                          <span className="nationality-name">
                            {player.nationality}
                          </span>
                        </>
                      )}
                    </div>
                    <p className="player-details">
                      {player.currentClubLogoUrl && (
                        <img
                          src={player.currentClubLogoUrl}
                          alt={player.currentClubName}
                          className="inline-club-badge"
                        />
                      )}
                      {player.currentClubName} • {player.position} •{" "}
                      {player.age}
                    </p>
                  </div>
                  <div className="trending-stats">
                    <span className="view-count">
                      {player.weeklyViews} views
                    </span>
                    <span className="trending-badge">
                      {player.weeklyGrowthPercentage !== undefined &&
                      player.weeklyGrowthPercentage !== null
                        ? `${
                            player.weeklyGrowthPercentage > 0 ? "+" : ""
                          }${Math.round(player.weeklyGrowthPercentage)}%`
                        : "0%"}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Best Players this Season */}
      <div className="dashboard-section col-span-8">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">
              <div className="info-tooltip">
                <span className="info-icon">ⓘ</span>
                <div className="tooltip-content">
                  Highest rated players with 1000+ minutes played this season
                </div>
              </div>
              🏆 Best Players this Season
            </h3>
            <button className="view-all-btn">View All</button>
          </div>
          <div className="card-content">
            <div className="top-players-season-grid">
              {displayedPlayers.map((player, index) => (
                <div key={player.id} className="top-player-season-item">
                  <div className="player-rank">#{index + 1}</div>
                  <div className="player-avatar">
                    {player.photoUrl ? (
                      <img src={player.photoUrl} alt={player.name} />
                    ) : (
                      <div className="avatar-placeholder">
                        {player.name.charAt(0)}
                      </div>
                    )}
                  </div>
                  <div className="player-info">
                    <h4 className="player-name">{player.name}</h4>
                    <div className="player-nationality">
                      {player.nationality && (
                        <>
                          <CountryFlag nationality={player.nationality} />
                          <span className="nationality-name">
                            {player.nationality}
                          </span>
                        </>
                      )}
                    </div>
                    <div className="player-club-league">
                      <div className="club-info">
                        {player.currentClubLogoUrl && (
                          <img
                            src={player.currentClubLogoUrl}
                            alt={player.currentClubName}
                            className="club-icon"
                          />
                        )}
                        <span className="club-name">
                          {player.currentClubName}
                        </span>
                      </div>
                    </div>
                    <p className="player-details">
                      {player.position} • {player.age} years
                    </p>
                  </div>
                  <div className="player-stats">
                    <span className="stat">
                      ⚽ <strong>{player.goals || 0}</strong>
                    </span>
                    <span className="stat">
                      🅰️ <strong>{player.assists || 0}</strong>
                    </span>
                    <span className="stat">
                      ⏱️ <strong>{player.minutesPlayed || 0}'</strong>
                    </span>
                  </div>
                  <div className="player-rating">
                    <div className="rating-score">
                      {player.rating ? player.rating.toFixed(1) : "N/A"}
                    </div>
                    <div className="rating-label">Rating</div>
                  </div>
                </div>
              ))}
            </div>
            {!showMorePlayers && topRatedPlayersSeason.length > 10 && (
              <div className="load-more-section">
                <button className="load-more-btn" onClick={loadMorePlayers}>
                  Load +{topRatedPlayersSeason.length - 10} more top performers
                </button>
              </div>
            )}
            {showMorePlayers && (
              <div className="load-more-section">
                <button
                  className="load-more-btn secondary"
                  onClick={() => setShowMorePlayers(false)}
                >
                  Show Less
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Top Rated Players */}
      <div className="dashboard-section col-span-4">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">
              <div className="info-tooltip">
                <span className="info-icon">ⓘ</span>
                <div className="tooltip-content">
                  Highest rated players based on coach and scout opinions
                </div>
              </div>
              ⭐ Top Rated Players
            </h3>
            <button className="view-all-btn">View All</button>
          </div>
          <div className="card-content">
            <div className="player-list">
              {topRatedPlayers.map((player) => (
                <div key={player.id} className="trending-player-item">
                  <div className="player-avatar">
                    {player.photoUrl ? (
                      <img src={player.photoUrl} alt={player.name} />
                    ) : (
                      <div className="avatar-placeholder">
                        {player.name.charAt(0)}
                      </div>
                    )}
                  </div>
                  <div className="player-info">
                    <h4 className="player-name">{player.name}</h4>
                    <p className="player-details">
                      {player.currentClubName} • {player.position} •{" "}
                      {player.age}
                    </p>
                  </div>
                  <div className="trending-stats">
                    <span className="rating-badge">
                      {player.averageRating
                        ? `${player.averageRating.toFixed(1)}/10`
                        : "N/A"}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Latest Reports */}
      <div className="dashboard-section col-span-4">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">
              <div className="info-tooltip">
                <span className="info-icon">ⓘ</span>
                <div className="tooltip-content">
                  Latest submitted scouting reports
                </div>
              </div>
              📊 Latest Scouting Reports
            </h3>
            <button className="view-all-btn">View All</button>
          </div>
          <div className="card-content">
            <div className="report-list">
              {latestReports.map((report) => (
                <div key={report.id} className="report-item">
                  <div className="report-icon">📋</div>
                  <div className="report-content">
                    <h4 className="report-title">{report.title}</h4>
                    <p className="report-player">Player: {report.playerName}</p>
                    <p className="report-scout">By: {report.scoutName}</p>
                  </div>
                  <div className="report-rating">
                    {report.overallRating && (
                      <span className="rating-badge">
                        {report.overallRating}/10
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Most Active Discussions */}
      <div className="dashboard-section col-span-4">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">
              <div className="info-tooltip">
                <span className="info-icon">ⓘ</span>
                <div className="tooltip-content">
                  Most popular active discussions
                </div>
              </div>
              💬 Most Active Discussions
            </h3>
            <button className="view-all-btn">View All</button>
          </div>
          <div className="card-content">
            <div className="discussion-list">
              {activeDiscussions.map((discussion) => (
                <div key={discussion.id} className="discussion-item">
                  <div className="discussion-icon">💬</div>
                  <div className="discussion-content">
                    <h4 className="discussion-title">{discussion.title}</h4>
                    <p className="discussion-meta">
                      By: {discussion.authorName} • {discussion.repliesCount}{" "}
                      replies
                    </p>
                  </div>
                  <div className="discussion-stats">
                    <span className="activity-badge">
                      {discussion.repliesCount}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Active Polls */}
      <div className="dashboard-section">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">
              <div className="info-tooltip">
                <span className="info-icon">ⓘ</span>
                <div className="tooltip-content">
                  Active Polls posted by users
                </div>
              </div>
              🗳️ Active Polls
            </h3>
            <button className="view-all-btn">View All</button>
          </div>
          <div className="card-content">
            <div className="poll-list">
              {activePolls.map((poll) => (
                <div key={poll.id} className="poll-item">
                  <div className="poll-content">
                    <h4 className="poll-title">{poll.title}</h4>
                    <p className="poll-meta">
                      {poll.totalVotes} votes • Ends:{" "}
                      {new Date(poll.expiresAt).toLocaleDateString()}
                    </p>
                    <div className="poll-options">
                      {poll.options.slice(0, 2).map((option) => (
                        <div key={option.id} className="poll-option">
                          <span className="option-text">
                            {option.optionText}
                          </span>
                          <span className="option-votes">
                            {option.voteCount} votes
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Your Activity */}
      {myActivity && (
        <div className="dashboard-section full-width">
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">
                <div className="info-tooltip">
                  <span className="info-icon">ⓘ</span>
                  <div className="tooltip-content">
                    Your recent activity on Talent Radar
                  </div>
                </div>
                📈 Your Recent Activity
              </h3>
            </div>
            <div className="card-content">
              <div className="activity-stats">
                <div className="stat-item">
                  <div className="stat-value">
                    {myActivity.weeklyViews || 0}
                  </div>
                  <div className="stat-label">Views This Week</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value">
                    {myActivity.totalReports || 0}
                  </div>
                  <div className="stat-label">Total Reports</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value">
                    {myActivity.recentViews?.length || 0}
                  </div>
                  <div className="stat-label">Recent Views</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value">
                    {myActivity.recentRatings?.length || 0}
                  </div>
                  <div className="stat-label">Recent Ratings</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// Enhanced Scout/Coach Dashboard
const ScoutDashboard = ({ scoutData, trendingPlayers, myActivity }) => (
  <div className="dashboard-grid scout-grid">
    {/* My Reports */}
    {scoutData && (
      <div className="dashboard-section">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">📋 Your Scouting Reports</h3>
            <button className="create-btn">Create New</button>
          </div>
          <div className="card-content">
            <div className="report-stats">
              <div className="stat-item">
                <div className="stat-value">{scoutData.publishedReports}</div>
                <div className="stat-label">Published</div>
              </div>
              <div className="stat-item">
                <div className="stat-value">{scoutData.draftReports}</div>
                <div className="stat-label">Drafts</div>
              </div>
            </div>
            <div className="report-list">
              {scoutData.recentReports?.slice(0, 5).map((report) => (
                <div key={report.id} className="my-report-item">
                  <div className="report-content">
                    <h4 className="report-title">{report.title}</h4>
                    <p className="report-player">{report.playerName}</p>
                  </div>
                  <div className="report-status">
                    <span
                      className={`status-badge status-${report.status.toLowerCase()}`}
                    >
                      {report.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    )}

    {/* Recently Added Players */}
    {scoutData && (
      <div className="dashboard-section">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">🆕 Recently Added Players</h3>
          </div>
          <div className="card-content">
            <div className="player-list">
              {scoutData.recentPlayers?.slice(0, 5).map((player) => (
                <div key={player.id} className="new-player-item">
                  <div className="player-info">
                    <h4 className="player-name">{player.name}</h4>
                    <p className="player-details">
                      {player.position} • {player.age} years •{" "}
                      {player.currentClubName}
                    </p>
                  </div>
                  <div className="player-meta">
                    <span className="new-badge">New</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    )}

    {/* Top Goal Scorers */}
    {scoutData && (
      <div className="dashboard-section">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">⚽ Top Goal Scorers</h3>
          </div>
          <div className="card-content">
            <div className="player-list">
              {scoutData.topGoalScorers?.map((player) => (
                <div key={player.id} className="stat-player-item">
                  <div className="player-info">
                    <h4 className="player-name">{player.name}</h4>
                    <p className="player-details">
                      {player.position} • {player.currentClubName}
                    </p>
                  </div>
                  <div className="player-stat">
                    <span className="stat-badge">
                      {player.goals || 0} goals
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    )}

    {/* Trending Players for Scouts */}
    <div className="dashboard-section">
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">📈 Trending Now</h3>
        </div>
        <div className="card-content">
          <div className="player-list">
            {trendingPlayers.map((player) => (
              <div key={player.id} className="trending-player-item">
                <div className="player-info">
                  <h4 className="player-name">{player.name}</h4>
                  <p className="player-details">
                    {player.position} • {player.currentClubName}
                  </p>
                </div>
                <div className="trending-stats">
                  <span className="view-count">{player.weeklyViews}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  </div>
);

// Admin Dashboard
const AdminDashboard = ({ adminData, platformStats }) => (
  <div className="dashboard-grid admin-grid">
    {/* Platform Stats */}
    {platformStats && (
      <div className="dashboard-section full-width">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">📊 Platform Statistics</h3>
          </div>
          <div className="card-content">
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-value">{platformStats.totalUsers}</div>
                <div className="stat-label">Total Users</div>
              </div>
              <div className="stat-card">
                <div className="stat-value">{platformStats.totalPlayers}</div>
                <div className="stat-label">Total Players</div>
              </div>
              <div className="stat-card">
                <div className="stat-value">{platformStats.totalReports}</div>
                <div className="stat-label">Scouting Reports</div>
              </div>
              <div className="stat-card">
                <div className="stat-value">{platformStats.totalClubs}</div>
                <div className="stat-label">Clubs</div>
              </div>
              <div className="stat-card">
                <div className="stat-value">{platformStats.viewsLast24h}</div>
                <div className="stat-label">Views (24h)</div>
              </div>
              <div className="stat-card">
                <div className="stat-value">{platformStats.reportsLast24h}</div>
                <div className="stat-label">Reports (24h)</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    )}

    {/* User Management */}
    {adminData && (
      <div className="dashboard-section">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">👥 User Management</h3>
          </div>
          <div className="card-content">
            <div className="admin-stats">
              <div className="stat-item">
                <div className="stat-value">{adminData.newUsersThisWeek}</div>
                <div className="stat-label">New This Week</div>
              </div>
            </div>
            <div className="user-list">
              <h4>Top Users by Reputation</h4>
              {adminData.topUsers?.slice(0, 5).map((user) => (
                <div key={user.id} className="top-user-item">
                  <div className="user-info">
                    <span className="username">{user.username}</span>
                    <span className="user-role">{user.role}</span>
                  </div>
                  <div className="reputation-score">
                    {user.reputationScore || 0}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    )}

    {/* Recent Activity */}
    {adminData && (
      <div className="dashboard-section">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">🕒 Recent Activity</h3>
          </div>
          <div className="card-content">
            <div className="activity-feed">
              {adminData.recentViews?.slice(0, 10).map((view, index) => (
                <div key={view.id || index} className="activity-item">
                  <div className="activity-icon">👁️</div>
                  <div className="activity-text">
                    <span className="username">
                      {view.user?.username || "Anonymous"}
                    </span>
                    <span> viewed </span>
                    <span className="player-name">{view.player?.name}</span>
                  </div>
                  <div className="activity-time">
                    {new Date(view.createdAt).toLocaleTimeString()}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    )}
  </div>
);

export default Dashboard;
