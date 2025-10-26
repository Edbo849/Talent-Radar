import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import Navbar from "./Navbar";
import { useAuth } from "../context/AuthContext";
import ApiService from "../services/api";
import FlagService from "../services/flagService";
import "./static/Dashboard.css";

const CountryFlag = ({ nationality, className = "nationality-flag" }) => {
  const [flag, setFlag] = useState(null);

  useEffect(() => {
    const loadFlag = async () => {
      await FlagService.loadCountries();
      const flagData = FlagService.getFlag(nationality);
      setFlag(flagData);
    };

    if (nationality) {
      loadFlag();
    }
  }, [nationality]);

  if (!flag) return null;

  if (flag.type === "url") {
    return (
      <img
        src={flag.value}
        alt={nationality}
        className={`${className} flag-image`}
        onError={(e) => {
          e.target.style.display = "none";
        }}
      />
    );
  }

  return <span className={className}>{flag.value}</span>;
};

// Helper function to get rating color class
const getRatingColorClass = (rating) => {
  if (!rating) return "rating-na";
  const numRating = parseFloat(rating);
  if (numRating < 5) return "rating-red";
  if (numRating < 6) return "rating-orange";
  if (numRating < 7) return "rating-yellow";
  if (numRating < 8) return "rating-green";
  if (numRating < 9) return "rating-light-blue";
  return "rating-dark-blue";
};

/**
 * Dashboard component - main application interface after login
 * Contains role-based content for different user types
 */
const Dashboard = () => {
  const { user, token } = useAuth();
  const navigate = useNavigate();
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

  const [selectedLeague, setSelectedLeague] = useState(null);
  const [leagues, setLeagues] = useState([]);
  const [leagueSearchQuery, setLeagueSearchQuery] = useState("");
  const [showLeagueDropdown, setShowLeagueDropdown] = useState(false);
  const [filteredPlayers, setFilteredPlayers] = useState([]);
  const [loadingLeagues, setLoadingLeagues] = useState(false);

  useEffect(() => {
    FlagService.loadCountries();
    loadDashboardData();
  }, [user, token]);

  useEffect(() => {
    loadLeagues();
  }, []);

  useEffect(() => {
    if (selectedLeague) {
      loadPlayersByLeague(selectedLeague.id);
    } else {
      setFilteredPlayers(dashboardData.topRatedPlayersSeason);
    }
  }, [selectedLeague, dashboardData.topRatedPlayersSeason]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (!event.target.closest(".league-filter-container")) {
        setShowLeagueDropdown(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const loadLeagues = async () => {
    try {
      setLoadingLeagues(true);
      const response = await ApiService.getAllLeagues();
      setLeagues(response.content || response);
    } catch (error) {
      console.error("Error loading leagues:", error);
    } finally {
      setLoadingLeagues(false);
    }
  };

  const loadPlayersByLeague = async (leagueId) => {
    try {
      const response = await ApiService.getTopRatedPlayersByLeague(
        leagueId,
        50
      );
      setFilteredPlayers(response);
    } catch (error) {
      console.error("Error loading players by league:", error);
      setFilteredPlayers([]);
    }
  };

  const handleLeagueSelect = (league) => {
    setSelectedLeague(league);
    setLeagueSearchQuery(league.name);
    setShowLeagueDropdown(false);
  };

  const clearLeagueFilter = () => {
    setSelectedLeague(null);
    setLeagueSearchQuery("");
    setFilteredPlayers(dashboardData.topRatedPlayersSeason);
  };

  const filteredLeagues = leagues.filter((league) =>
    league.name.toLowerCase().includes(leagueSearchQuery.toLowerCase())
  );

  const currentPlayers = selectedLeague
    ? filteredPlayers
    : dashboardData.topRatedPlayersSeason;
  const displayedPlayers = dashboardData.showMorePlayers
    ? currentPlayers
    : currentPlayers.slice(0, 10);

  // Function to navigate to player page
  const handlePlayerClick = (playerId) => {
    navigate(`/player/${playerId}`);
  };

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
            onPlayerClick={handlePlayerClick}
          />
        ) : user.role === "SCOUT" || user.role === "COACH" ? (
          <ScoutDashboard
            scoutData={dashboardData.scoutData}
            trendingPlayers={dashboardData.trendingPlayers}
            myActivity={dashboardData.myActivity}
            onPlayerClick={handlePlayerClick}
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
            selectedLeague={selectedLeague}
            leagues={leagues}
            leagueSearchQuery={leagueSearchQuery}
            showLeagueDropdown={showLeagueDropdown}
            filteredPlayers={filteredPlayers}
            loadingLeagues={loadingLeagues}
            filteredLeagues={filteredLeagues}
            setLeagueSearchQuery={setLeagueSearchQuery}
            setShowLeagueDropdown={setShowLeagueDropdown}
            handleLeagueSelect={handleLeagueSelect}
            clearLeagueFilter={clearLeagueFilter}
            onPlayerClick={handlePlayerClick}
            onLoadMorePlayers={loadMorePlayers}
          />
        )}
      </div>
    </div>
  );
};

// General User Dashboard
const UserDashboard = ({
  trendingPlayers,
  topRatedPlayers,
  latestReports,
  activeDiscussions,
  activePolls,
  myActivity,
  topRatedPlayersSeason,
  selectedLeague,
  leagues,
  leagueSearchQuery,
  showLeagueDropdown,
  filteredPlayers,
  loadingLeagues,
  filteredLeagues,
  setLeagueSearchQuery,
  setShowLeagueDropdown,
  handleLeagueSelect,
  clearLeagueFilter,
  onPlayerClick,
  onLoadMorePlayers,
}) => {
  const [showMorePlayers, setShowMorePlayers] = useState(false);
  const [allPlayers, setAllPlayers] = useState(topRatedPlayersSeason);

  const loadMorePlayers = async () => {
    try {
      await onLoadMorePlayers();
      setShowMorePlayers(true);
    } catch (error) {
      console.error("Error loading more players:", error);
    }
  };
  const currentPlayers = selectedLeague
    ? filteredPlayers
    : topRatedPlayersSeason;
  const displayedPlayers = showMorePlayers
    ? currentPlayers
    : currentPlayers.slice(0, 10);

  return (
    <div className="dashboard-grid">
      {/* Trending Players */}
      <div className="dashboard-section col-span-4">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">🔥 Trending Players</h3>
            <button className="view-all-btn">View All</button>
          </div>
          <div className="card-content">
            <div className="player-list">
              {trendingPlayers.map((player) => (
                <div
                  key={player.id}
                  className="trending-player-item"
                  onClick={() => onPlayerClick(player.id)}
                >
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
                    <h4 className="player-name-dash">{player.name}</h4>
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
            <h3 className="card-title">🏆 Best Players this Season</h3>
            <div className="header-controls">
              <div className="league-filter-container">
                <div className="league-search-wrapper">
                  <input
                    type="text"
                    placeholder="Search leagues..."
                    value={leagueSearchQuery}
                    onChange={(e) => {
                      setLeagueSearchQuery(e.target.value);
                      setShowLeagueDropdown(true);
                    }}
                    onFocus={() => setShowLeagueDropdown(true)}
                    className="league-search-input"
                  />
                  {selectedLeague && (
                    <button
                      onClick={clearLeagueFilter}
                      className="clear-filter-btn"
                      title="Clear filter"
                    >
                      ✕
                    </button>
                  )}
                  {showLeagueDropdown && (
                    <div className="league-dropdown">
                      {loadingLeagues ? (
                        <div className="dropdown-loading">
                          Loading leagues...
                        </div>
                      ) : (
                        <>
                          {!selectedLeague && (
                            <div
                              className="dropdown-item"
                              onClick={() => {
                                clearLeagueFilter();
                                setShowLeagueDropdown(false);
                              }}
                            >
                              <span>All Leagues</span>
                            </div>
                          )}
                          {filteredLeagues.slice(0, 10).map((league) => (
                            <div
                              key={league.id}
                              className="dropdown-item"
                              onClick={() => handleLeagueSelect(league)}
                            >
                              {league.logoUrl && (
                                <img
                                  src={league.logoUrl}
                                  alt={league.name}
                                  className="league-logo-small"
                                />
                              )}
                              <span>{league.name}</span>
                              {league.country && (
                                <span className="league-country">
                                  {league.country.name}
                                </span>
                              )}
                            </div>
                          ))}
                          {filteredLeagues.length === 0 && (
                            <div className="dropdown-item disabled">
                              No leagues found
                            </div>
                          )}
                        </>
                      )}
                    </div>
                  )}
                </div>
              </div>
              <button className="view-all-btn">View All</button>
            </div>
          </div>
          <div className="card-content">
            <div className="top-players-season-grid">
              {displayedPlayers.map((player, index) => (
                <div
                  key={player.id}
                  className="top-player-season-item clickable-player"
                  onClick={() => onPlayerClick(player.id)}
                >
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
                    <h4 className="player-name-dash">{player.name}</h4>
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
                    <div
                      className={`rating-score ${getRatingColorClass(
                        player.rating
                      )}`}
                    >
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
            <h3 className="card-title">⭐ Top Rated Players</h3>
            <button className="view-all-btn">View All</button>
          </div>
          <div className="card-content">
            <div className="player-list">
              {topRatedPlayers.map((player) => (
                <div
                  key={player.id}
                  className="trending-player-item clickable-player"
                  onClick={() => onPlayerClick(player.id)}
                >
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
                    <h4 className="player-name-dash">{player.name}</h4>
                    <p className="player-details">
                      {player.currentClubName} • {player.position} •{" "}
                      {player.age}
                    </p>
                  </div>
                  <div className="trending-stats">
                    <span className="rating-badge">
                      {player.averageRating
                        ? player.averageRating.toFixed(1)
                        : "N/A"}{" "}
                      ⭐
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Latest Scouting Reports */}
      <div className="dashboard-section col-span-8">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">📋 Latest Scouting Reports</h3>
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
            <h3 className="card-title">💬 Most Active Discussions</h3>
            <button className="view-all-btn">View All</button>
          </div>
          <div className="card-content">
            <div className="discussion-list">
              {activeDiscussions.map((discussion) => (
                <div key={discussion.id} className="discussion-item">
                  <div className="discussion-icon">💬</div>
                  <div className="discussion-content">
                    <h4 className="discussion-title">{discussion.title}</h4>
                    <div className="discussion-meta">
                      <span>{discussion.repliesCount || 0} replies</span> •{" "}
                      <span>{discussion.viewsCount || 0} views</span>
                    </div>
                  </div>
                  <div className="activity-badge">
                    {discussion.lastActivityHours || 0}h
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Active Polls */}
      <div className="dashboard-section col-span-8">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">🗳️ Active Polls</h3>
            <button className="view-all-btn">View All</button>
          </div>
          <div className="card-content">
            <div className="poll-list">
              {activePolls.map((poll) => (
                <div key={poll.id} className="poll-item">
                  <h4 className="poll-title">{poll.title}</h4>
                  <div className="poll-meta">
                    <span>{poll.totalVotes || 0} votes</span> •{" "}
                    <span>
                      {poll.daysRemaining
                        ? `${poll.daysRemaining} days left`
                        : "Ending soon"}
                    </span>
                  </div>
                  <div className="poll-options">
                    {poll.options?.slice(0, 2).map((option, index) => (
                      <div key={index} className="poll-option">
                        <span className="option-text">{option.text}</span>
                        <span className="option-votes">
                          {(
                            (option.votes / poll.totalVotes) * 100 || 0
                          ).toFixed(0)}
                          %
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Scout/Coach Dashboard
const ScoutDashboard = ({
  scoutData,
  trendingPlayers,
  myActivity,
  onPlayerClick,
}) => (
  <div className="dashboard-grid scout-grid">
    {/* Scouting Reports */}
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
                <div
                  key={player.id}
                  className="new-player-item clickable-player"
                  onClick={() => onPlayerClick(player.id)}
                >
                  <div className="player-info">
                    <h4 className="player-name-dash">{player.name}</h4>
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
                <div
                  key={player.id}
                  className="stat-player-item clickable-player"
                  onClick={() => onPlayerClick(player.id)}
                >
                  <div className="player-info">
                    <h4 className="player-name-dash">{player.name}</h4>
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
              <div
                key={player.id}
                className="trending-player-item clickable-player"
                onClick={() => onPlayerClick(player.id)}
              >
                <div className="player-info">
                  <h4 className="player-name-dash">{player.name}</h4>
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
const AdminDashboard = ({ adminData, platformStats, onPlayerClick }) => (
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
                    <span
                      className="player-name-dash clickable-text"
                      onClick={() => onPlayerClick(view.player?.id)}
                    >
                      {view.player?.name}
                    </span>
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
