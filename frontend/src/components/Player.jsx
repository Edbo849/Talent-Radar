import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Navbar from "./Navbar";
import ApiService from "../services/api";
import FlagService from "../services/flagService";
import "./static/Player.css";

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
          e.target.nextSibling?.style &&
            (e.target.nextSibling.style.display = "inline");
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

const PlayerPage = () => {
  const { playerId } = useParams();
  const navigate = useNavigate();
  const [playerData, setPlayerData] = useState({
    player: null,
    statistics: [],
    transfers: [],
    sidelined: [],
    trophies: [],
    comments: [],
    ratings: [],
    loading: true,
    error: null,
  });

  useEffect(() => {
    loadPlayerData();
    trackPlayerView();
  }, [playerId]);

  const loadPlayerData = async () => {
    try {
      setPlayerData((prev) => ({ ...prev, loading: true, error: null }));

      const [
        player,
        statistics,
        transfers,
        sidelined,
        trophies,
        comments,
        ratings,
      ] = await Promise.all([
        ApiService.getPlayer(playerId),
        ApiService.getPlayerStatistics(playerId).catch(() => []),
        ApiService.getPlayerTransfers(playerId).catch(() => []),
        ApiService.getPlayerSidelined(playerId).catch(() => []),
        ApiService.getPlayerTrophies(playerId).catch(() => []),
        ApiService.getPlayerComments(playerId).catch(() => ({ content: [] })),
        ApiService.getPlayerRatings(playerId).catch(() => ({ content: [] })),
      ]);

      setPlayerData({
        player,
        statistics,
        transfers,
        sidelined,
        trophies,
        comments: comments.content || [],
        ratings: ratings.content || [],
        loading: false,
        error: null,
      });
    } catch (error) {
      console.error("Error loading player data:", error);
      setPlayerData((prev) => ({
        ...prev,
        loading: false,
        error: "Failed to load player data",
      }));
    }
  };

  const trackPlayerView = async () => {
    try {
      await fetch(`http://localhost:8080/api/players/${playerId}/views`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...(localStorage.getItem("token") && {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          }),
        },
      });
      console.log("Player view tracked successfully");
    } catch (error) {
      console.error("Error tracking player view:", error);
    }
  };

  if (playerData.loading) {
    return (
      <div className="player-page-container">
        <Navbar />
        <div className="player-page-content">
          <LoadingSkeleton />
        </div>
      </div>
    );
  }

  if (playerData.error || !playerData.player) {
    return (
      <div className="player-page-container">
        <Navbar />
        <div className="player-page-content">
          <div className="empty-state">
            <div className="empty-state-icon">❌</div>
            <h3 className="empty-state-title">Player Not Found</h3>
            <p className="empty-state-message">
              {playerData.error ||
                "The player you are looking for does not exist."}
            </p>
            <button
              onClick={() => {
                if (window.history.length > 1) {
                  navigate(-1);
                } else {
                  navigate("/");
                }
              }}
              className="load-more-btn"
            >
              Go Back
            </button>
          </div>
        </div>
      </div>
    );
  }

  const {
    player,
    statistics,
    transfers,
    sidelined,
    trophies,
    comments,
    ratings,
  } = playerData;

  return (
    <div className="player-page-container">
      <Navbar />
      <div className="player-page-content">
        {/* MAIN PLAYER INFORMATION SECTION */}
        <div className="player-info-section">
          <div className="player-info-grid-layout">
            {/* Player Profile */}
            <div className="player-profile-grid-item">
              <PlayerProfileCard player={player} />
            </div>

            {/* Personal Details */}
            <div className="personal-details-grid-item">
              <PersonalDetailsSection player={player} />
            </div>

            {/* Trophies */}
            <div className="trophies-grid-item">
              <TrophiesSection trophies={trophies} />
            </div>

            {/* Career Statistics */}
            <div className="career-stats-grid-item">
              <StatisticsSection statistics={statistics} />
            </div>

            {/* Advanced Statistics */}
            <div className="advanced-stats-grid-item">
              <AdvancedStatisticsSection
                statistics={statistics}
                player={player}
              />
            </div>

            {/* Transfers */}
            <div className="transfers-grid-item">
              <TransfersSection transfers={transfers} />
            </div>

            {/* Injuries (from sidelined endpoint) */}
            <div className="performance-trends-grid-item">
              <InjuriesSection sidelined={sidelined} />
            </div>
          </div>
        </div>

        {/* COMMUNITY & SOCIAL SECTION */}
        <div className="player-community-section">
          <h2 className="section-divider-title">Community & Ratings</h2>

          <div className="community-grid">
            <div className="community-grid-item">
              <CommentsSection comments={comments} playerId={playerId} />
            </div>
            <div className="community-grid-item">
              <RatingsSection ratings={ratings} />
            </div>
            <div className="community-grid-item full-width">
              <ScoutingReportsSection playerId={playerId} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Player Profile Card Component
const PlayerProfileCard = ({ player }) => {
  const calculateAge = (dateOfBirth) => {
    if (!dateOfBirth) return "Unknown";
    const today = new Date();
    const birth = new Date(dateOfBirth);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();

    if (
      monthDiff < 0 ||
      (monthDiff === 0 && today.getDate() < birth.getDate())
    ) {
      age--;
    }

    return age;
  };

  return (
    <div className="player-section tall player-profile-card">
      <div className="section-header">
        <h3 className="section-title">
          <span>👤</span>
          Player Profile
        </h3>
      </div>
      <div className="section-content">
        <div className="player-profile-content-enhanced">
          <div className="player-photo-section-enhanced">
            {player.photoUrl ? (
              <img
                src={player.photoUrl}
                alt={player.name}
                className="player-photo-enhanced"
              />
            ) : (
              <div className="player-photo-placeholder-enhanced">
                {player.name?.charAt(0) || "?"}
              </div>
            )}
            {player.jerseyNumber && (
              <div className="player-jersey-number-enhanced">
                #{player.jerseyNumber}
              </div>
            )}
          </div>

          <div className="player-basic-info-enhanced">
            <h1 className="player-name-enhanced">
              {player.firstName} {player.lastName}
            </h1>
            <div className="player-position-age-enhanced">
              {player.position} • {calculateAge(player.dateOfBirth)} years old
            </div>

            {player.nationality && (
              <div className="player-nationality-section-enhanced">
                <CountryFlag
                  nationality={player.nationality}
                  className="nationality-flag-enhanced"
                />
                <span className="nationality-text-enhanced">
                  {player.nationality}
                </span>
              </div>
            )}

            {player.currentClubName && (
              <div className="player-club-info-enhanced">
                {player.currentClubLogoUrl && (
                  <img
                    src={player.currentClubLogoUrl}
                    alt={player.currentClubName}
                    className="current-club-logo-enhanced"
                  />
                )}
                <span className="current-club-name-enhanced">
                  {player.currentClubName}
                </span>
              </div>
            )}

            <div className="player-status-badges-enhanced">
              {player.isActive && (
                <span className="status-badge status-active">Active</span>
              )}
              {player.isInjured && (
                <span className="status-badge status-injured">Injured</span>
              )}
              {player.isEligibleForU21 && (
                <span className="status-badge status-u21">U21 Eligible</span>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Personal Details Section
const PersonalDetailsSection = ({ player }) => {
  const formatDate = (date) => {
    if (!date) return "Unknown";
    return new Date(date).toLocaleDateString("en-GB", {
      day: "numeric",
      month: "long",
      year: "numeric",
    });
  };

  const formatHeight = (heightCm) => {
    if (!heightCm) return "N/A";
    const feet = Math.floor(heightCm / 30.48);
    const inches = Math.round((heightCm / 30.48 - feet) * 12);
    return `${heightCm}cm (${feet}'${inches}")`;
  };

  return (
    <div className="player-section compact">
      <div className="section-header">
        <h3 className="section-title">
          <span>📋</span>
          Personal Details
        </h3>
      </div>
      <div className="section-content">
        <div className="personal-details-compact">
          <div className="detail-row">
            <span className="detail-label">Date of Birth</span>
            <span className="detail-value">
              {formatDate(player.dateOfBirth)}
            </span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Height</span>
            <span className="detail-value">
              {formatHeight(player.heightCm)}
            </span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Weight</span>
            <span className="detail-value">
              {player.weightKg ? `${player.weightKg}kg` : "N/A"}
            </span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Birth Place</span>
            <span className="detail-value">{player.birthPlace || "N/A"}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Birth Country</span>
            <span className="detail-value">{player.birthCountry || "N/A"}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Jersey Number</span>
            <span className="detail-value">{player.jerseyNumber || "N/A"}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

const StatisticsSection = ({ statistics }) => {
  const [expandedSeason, setExpandedSeason] = useState(null);

  if (statistics.length === 0) {
    return (
      <div className="player-section">
        <div className="section-header">
          <h3 className="section-title">
            <span>📊</span>
            Career Statistics
          </h3>
        </div>
        <div className="section-content">
          <div className="empty-state-small">
            <div className="empty-state-icon">📊</div>
            <p className="empty-state-message">No statistics available</p>
          </div>
        </div>
      </div>
    );
  }

  // Filter out stats with 0 appearances
  const validStatistics = statistics.filter(
    (stat) => (stat.appearances || 0) > 0
  );

  // Calculate career totals
  const careerTotals = validStatistics.reduce((totals, stat) => {
    return {
      appearances: (totals.appearances || 0) + (stat.appearances || 0),
      goals: (totals.goals || 0) + (stat.goals || 0),
      assists: (totals.assists || 0) + (stat.assists || 0),
      minutesPlayed: (totals.minutesPlayed || 0) + (stat.minutesPlayed || 0),
    };
  }, {});

  const averageRating = (() => {
    const validRatings = validStatistics.filter(
      (stat) => stat.rating && parseFloat(stat.rating) > 0
    );

    if (validRatings.length === 0) return 0;

    const sum = validRatings.reduce((total, stat) => {
      return total + parseFloat(stat.rating);
    }, 0);

    return sum / validRatings.length;
  })();

  // Group statistics by season
  const statsBySeason = validStatistics.reduce((acc, stat) => {
    const season = stat.season || "Unknown";
    if (!acc[season]) acc[season] = [];
    acc[season].push(stat);
    return acc;
  }, {});

  // Sort seasons descending
  const seasons = Object.keys(statsBySeason).sort((a, b) => b - a);

  const toggleSeason = (season) => {
    setExpandedSeason(expandedSeason === season ? null : season);
  };

  return (
    <div className="player-section">
      <div className="section-header">
        <h3 className="section-title">
          <span>📊</span>
          Career Statistics
        </h3>
      </div>
      <div className="section-content">
        {/* Season Breakdown Table */}
        <div className="seasons-breakdown">
          <div className="stats-table">
            <div className="stats-table-header">
              <span className="col-year">Year</span>
              <span className="col-team">Team</span>
              <span className="col-appearances">MP</span>
              <span className="col-goals">GLS</span>
              <span className="col-assists">AST</span>
              <span className="col-rating">Rating</span>
            </div>

            {/* Career Totals Row */}
            <div className="season-row-container career-totals-row">
              <div className="season-row career-totals">
                <span className="col-year career-label">Career</span>
                <span className="col-team career-team">All Teams</span>
                <span className="col-appearances career-stat">
                  {careerTotals.appearances || 0}
                </span>
                <span className="col-goals career-stat">
                  {careerTotals.goals || 0}
                </span>
                <span className="col-assists career-stat">
                  {careerTotals.assists || 0}
                </span>
                <span
                  className={`col-rating career-stat ${getRatingColorClass(
                    averageRating
                  )}`}
                >
                  {averageRating ? averageRating.toFixed(1) : "N/A"}
                </span>
              </div>
            </div>

            {seasons.map((season) => {
              const seasonStats = statsBySeason[season];
              const seasonTotals = seasonStats.reduce((totals, stat) => {
                return {
                  appearances:
                    (totals.appearances || 0) + (stat.appearances || 0),
                  goals: (totals.goals || 0) + (stat.goals || 0),
                  assists: (totals.assists || 0) + (stat.assists || 0),
                };
              }, {});

              const seasonAvgRating = (() => {
                const validRatings = seasonStats.filter(
                  (stat) => stat.rating && parseFloat(stat.rating) > 0
                );

                if (validRatings.length === 0) return 0;

                const sum = validRatings.reduce((total, stat) => {
                  return total + parseFloat(stat.rating);
                }, 0);

                return sum / validRatings.length;
              })();

              // Get unique teams for this season
              const uniqueTeams = [
                ...new Set(
                  seasonStats.map((stat) => stat.clubName).filter(Boolean)
                ),
              ];

              return (
                <div key={season} className="season-row-container">
                  <div
                    className={`season-row ${
                      expandedSeason === season ? "expanded" : ""
                    }`}
                    onClick={() => toggleSeason(season)}
                  >
                    <span className="col-year">
                      {season.toString().slice(-2)}/
                      {(parseInt(season) + 1).toString().slice(-2)}
                    </span>
                    <span className="col-team">
                      <div className="team-badges">
                        {uniqueTeams
                          .slice(0, uniqueTeams.length)
                          .map((teamName, idx) => {
                            const teamStat = seasonStats.find(
                              (stat) => stat.clubName === teamName
                            );
                            return (
                              <div key={idx} className="team-badge">
                                {teamStat?.clubLogoUrl && (
                                  <img
                                    src={teamStat.clubLogoUrl}
                                    alt={teamName}
                                    className="team-logo-small"
                                  />
                                )}
                              </div>
                            );
                          })}
                      </div>
                    </span>
                    <span className="col-appearances">
                      {seasonTotals.appearances}
                    </span>
                    <span className="col-goals">{seasonTotals.goals}</span>
                    <span className="col-assists">{seasonTotals.assists}</span>
                    <span
                      className={`col-rating ${getRatingColorClass(
                        seasonAvgRating
                      )}`}
                    >
                      {seasonAvgRating ? seasonAvgRating.toFixed(1) : "N/A"}
                    </span>
                  </div>

                  {expandedSeason === season && (
                    <div className="season-details-table">
                      {seasonStats.map((stat, index) => (
                        <div key={index} className="competition-row">
                          <span className="col-year"></span>
                          <span className="col-team">
                            <div className="team-competition-info">
                              <div className="competition-info">
                                {stat.leagueLogoUrl && (
                                  <img
                                    src={stat.leagueLogoUrl}
                                    alt={stat.leagueName}
                                    className="league-logo-small"
                                  />
                                )}
                                <span className="league-name">
                                  {stat.leagueName || "Unknown League"}
                                </span>
                              </div>
                            </div>
                          </span>
                          <span className="col-appearances">
                            {stat.appearances || 0}
                          </span>
                          <span className="col-goals">{stat.goals || 0}</span>
                          <span className="col-assists">
                            {stat.assists || 0}
                          </span>
                          <span
                            className={`col-rating ${getRatingColorClass(
                              stat.rating
                            )}`}
                          >
                            {stat.rating
                              ? parseFloat(stat.rating).toFixed(1)
                              : "N/A"}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};

const TransfersSection = ({ transfers }) => {
  if (!transfers || transfers.length === 0) {
    return (
      <div className="player-section compact">
        <div className="section-header">
          <h3 className="section-title">
            <span>🔄</span>
            Transfer History
          </h3>
        </div>
        <div className="section-content">
          <div className="empty-state-small">
            <div className="empty-state-icon">🔄</div>
            <p className="empty-state-message">No transfers recorded</p>
          </div>
        </div>
      </div>
    );
  }

  const recentTransfers = transfers.slice(0, 5);

  const getTransferTypeColor = (type) => {
    switch (type?.toLowerCase()) {
      case "loan":
        return "transfer-type-loan";
      case "free":
        return "transfer-type-free";
      case "permanent":
        return "transfer-type-permanent";
      default:
        return "transfer-type-default";
    }
  };

  return (
    <div className="player-section compact">
      <div className="section-header">
        <h3 className="section-title">
          <span>🔄</span>
          Transfer History
        </h3>
      </div>
      <div className="section-content">
        <div className="transfers-list-enhanced">
          {recentTransfers.map((transfer, index) => (
            <div key={transfer.id || index} className="transfer-item-enhanced">
              <div className="transfer-date-enhanced">
                {new Date(transfer.transferDate).toLocaleDateString()}
              </div>

              <div className="transfer-clubs-enhanced">
                <div className="club-section">
                  {transfer.fromClubLogoUrl && (
                    <img
                      src={transfer.fromClubLogoUrl}
                      alt={transfer.fromClub}
                      className="club-logo-small"
                    />
                  )}
                  <span className="from-club">
                    {transfer.fromClub || "Youth"}
                  </span>
                </div>

                <span className="transfer-arrow">→</span>

                <div className="club-section">
                  {transfer.toClubLogoUrl && (
                    <img
                      src={transfer.toClubLogoUrl}
                      alt={transfer.toClub}
                      className="club-logo-small"
                    />
                  )}
                  <span className="to-club">{transfer.toClub}</span>
                </div>
              </div>

              <div className="transfer-meta">
                {transfer.transferType && (
                  <span
                    className={`transfer-type ${getTransferTypeColor(
                      transfer.transferType
                    )}`}
                  >
                    {transfer.transferType}
                  </span>
                )}
                {transfer.transferFee && (
                  <span className="transfer-fee-enhanced">
                    €{transfer.transferFee.toLocaleString()}
                  </span>
                )}
              </div>
            </div>
          ))}

          {transfers.length > 5 && (
            <div className="view-all-transfers">
              <button className="view-all-btn">
                View All {transfers.length} Transfers
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

// Trophies Section
const TrophiesSection = ({ trophies }) => {
  if (!trophies || trophies.length === 0) {
    return (
      <div className="player-section compact">
        <div className="section-header">
          <h3 className="section-title">
            <span>🏆</span>
            Trophies & Awards
          </h3>
        </div>
        <div className="section-content">
          <div className="empty-state-small">
            <div className="empty-state-icon">🏆</div>
            <p className="empty-state-message">No trophies recorded</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="player-section compact">
      <div className="section-header">
        <h3 className="section-title">
          <span>🏆</span>
          Trophies & Awards
        </h3>
      </div>
      <div className="section-content">
        <div className="trophies-list-compact">
          {trophies.slice(0, 4).map((trophy, index) => (
            <div key={trophy.id || index} className="trophy-item-compact">
              <div className="trophy-icon-compact">🏆</div>
              <div className="trophy-details-compact">
                <span className="trophy-name">{trophy.leagueName}</span>
                <span className="trophy-year">{trophy.season}</span>
              </div>
            </div>
          ))}
          {trophies.length > 4 && (
            <div className="view-all-trophies">
              <button className="view-all-btn">
                View All {trophies.length} Trophies
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

const InjuriesSection = ({ sidelined }) => {
  if (!sidelined || sidelined.length === 0) {
    return (
      <div className="player-section compact">
        <div className="section-header">
          <h3 className="section-title">
            <span>🏥</span>
            Injury History
          </h3>
        </div>
        <div className="section-content">
          <div className="empty-state-small">
            <div className="empty-state-icon">✅</div>
            <p className="empty-state-message">No injuries recorded</p>
          </div>
        </div>
      </div>
    );
  }

  // Sort sidelined periods by start date (most recent first)
  const sortedSidelined = [...sidelined].sort((a, b) => {
    if (!a.startDate && !b.startDate) return 0;
    if (!a.startDate) return 1;
    if (!b.startDate) return -1;
    return new Date(b.startDate) - new Date(a.startDate);
  });

  const formatDate = (dateString) => {
    if (!dateString) return null;
    return new Date(dateString).toLocaleDateString("en-GB", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  };

  const isActive = (injury) => {
    return !injury.endDate || new Date(injury.endDate) > new Date();
  };

  const getStatusIcon = (injury) => {
    if (isActive(injury)) {
      return "🔴"; // Active injury
    }
    return "🟢"; // Recovered
  };

  const getStatusText = (injury) => {
    if (isActive(injury)) {
      return "Active";
    }
    return "Recovered";
  };

  const getDaysCount = (injury) => {
    const startDate = new Date(injury.startDate);
    const endDate = injury.endDate ? new Date(injury.endDate) : new Date();
    const diffTime = Math.abs(endDate - startDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  return (
    <div className="player-section compact">
      <div className="section-header">
        <h3 className="section-title">
          <span>🏥</span>
          Injury History
        </h3>
      </div>
      <div className="section-content">
        <div className="injuries-list-enhanced">
          {sortedSidelined.slice(0, 5).map((injury, index) => (
            <div key={injury.id || index} className="injury-item-enhanced">
              <div className="injury-main-info">
                <div className="injury-status-section">
                  <div
                    className={`injury-status-icon ${
                      isActive(injury) ? "status-active" : "status-recovered"
                    }`}
                  >
                    {getStatusIcon(injury)}
                  </div>
                  <div className="injury-status-details">
                    <span className="injury-type">
                      {injury.type || "Injury/Suspension"}
                    </span>
                    <span
                      className={`injury-status-text ${
                        isActive(injury) ? "text-active" : "text-recovered"
                      }`}
                    >
                      {getStatusText(injury)}
                    </span>
                  </div>
                </div>

                <div className="injury-duration">
                  <span className="duration-text">
                    {getDaysCount(injury)} days
                  </span>
                </div>
              </div>

              <div className="injury-dates">
                <div className="date-section">
                  <span className="date-label">Start:</span>
                  <span className="date-value">
                    {formatDate(injury.startDate) || "Unknown"}
                  </span>
                </div>
                {injury.endDate && (
                  <div className="date-section">
                    <span className="date-label">End:</span>
                    <span className="date-value">
                      {formatDate(injury.endDate)}
                    </span>
                  </div>
                )}
                {!injury.endDate && (
                  <div className="date-section">
                    <span className="date-label">End:</span>
                    <span className="date-value ongoing">Ongoing</span>
                  </div>
                )}
              </div>
            </div>
          ))}

          {sidelined.length > 5 && (
            <div className="view-all-injuries">
              <button className="view-all-btn">
                View All {sidelined.length} Records
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

const AdvancedStatisticsSection = ({ statistics, player }) => {
  const [selectedSeason, setSelectedSeason] = useState(null);
  const [isPer90, setIsPer90] = useState(false);

  // Get unique seasons from statistics
  const seasons = [...new Set(statistics.map((stat) => stat.season))]
    .filter((season) => season)
    .sort((a, b) => b - a);

  // Set default season to latest
  useEffect(() => {
    if (seasons.length > 0 && !selectedSeason) {
      setSelectedSeason(seasons[0]);
    }
  }, [seasons]);

  // Filter statistics for selected season
  const seasonStats = statistics.filter(
    (stat) => stat.season === selectedSeason
  );

  // Calculate totals for the season
  const totals = seasonStats.reduce((acc, stat) => {
    Object.keys(stat).forEach((key) => {
      if (typeof stat[key] === "number" && key !== "season" && key !== "id") {
        // Special handling for rating - calculate weighted average
        if (key === "rating" && stat[key] && (stat.appearances || 0) > 0) {
          acc.totalRatingPoints =
            (acc.totalRatingPoints || 0) + stat[key] * (stat.appearances || 0);
          acc.totalAppearancesForRating =
            (acc.totalAppearancesForRating || 0) + (stat.appearances || 0);
        }
        // Regular sum for other stats
        else if (key !== "rating") {
          acc[key] = (acc[key] || 0) + stat[key];
        }
      }
    });
    return acc;
  }, {});

  // Calculate the weighted average rating
  if (totals.totalRatingPoints && totals.totalAppearancesForRating > 0) {
    totals.rating = totals.totalRatingPoints / totals.totalAppearancesForRating;
  }

  // Calculate minutes per game
  if (totals.appearances && totals.appearances > 0 && totals.minutesPlayed) {
    totals.minutesPerGame = totals.minutesPlayed / totals.appearances;
  } else {
    totals.minutesPerGame = 0;
  }

  // Calculate per 90 stats
  const per90Stats = {};
  if (totals.minutesPlayed && totals.minutesPlayed > 0) {
    const gamesEquivalent = totals.minutesPlayed / 90;
    Object.keys(totals).forEach((key) => {
      if (
        typeof totals[key] === "number" &&
        key !== "minutesPlayed" &&
        key !== "appearances"
      ) {
        per90Stats[key] = totals[key] / gamesEquivalent;
      }
    });
  }

  const displayStats = isPer90 ? per90Stats : totals;

  const isGoalkeeper =
    player.position?.toLowerCase().includes("goalkeeper") ||
    player.position?.toLowerCase().includes("gk");

  const formatStat = (value, decimals = 1) => {
    if (value === undefined || value === null) return "0";
    return isPer90 ? Number(value).toFixed(decimals) : Math.round(value);
  };

  const formatSeasonDisplay = (season) => {
    if (!season) return "Unknown";
    return `${season}/${(season + 1).toString().slice(-2)}`;
  };

  // Calculate substitutions
  const totalSubs = totals.substitutesIn || 0;
  const totalAppearances = totals.appearances || 0;
  const startingAppearances = totalAppearances - totalSubs;

  return (
    <div className="player-section">
      <div className="section-header">
        <h3 className="section-title">
          <span>📊</span>
          Advanced Statistics
        </h3>
        <div className="advanced-stats-controls">
          <select
            value={selectedSeason || ""}
            onChange={(e) => setSelectedSeason(parseInt(e.target.value))}
            className="season-selector"
          >
            {seasons.map((season) => (
              <option key={season} value={season}>
                {formatSeasonDisplay(season)}
              </option>
            ))}
          </select>
          <button
            className={`stats-toggle ${isPer90 ? "active" : ""}`}
            onClick={() => setIsPer90(!isPer90)}
          >
            {isPer90 ? "Per 90" : "Total"}
          </button>
        </div>
      </div>
      <div className="section-content">
        {!selectedSeason || seasonStats.length === 0 ? (
          <div className="empty-state-small">
            <div className="empty-state-icon">📊</div>
            <p className="empty-state-message">
              No statistics available for selected season
            </p>
          </div>
        ) : (
          <div className="advanced-stats-content">
            {/* General Stats */}
            <div className="stats-section general-stats">
              <h4 className="stats-section-title">General</h4>
              <div className="stats-grid">
                <div className="stat-item">
                  <span className="stat-label">Appearances:</span>
                  <span className="stat-value">
                    {totalAppearances} {totalSubs > 0 && `(${totalSubs})`}
                  </span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Minutes:</span>
                  <span className="stat-value">
                    {totals.minutesPlayed || 0}
                  </span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Minutes per Game:</span>
                  <span className="stat-value">
                    {totals.minutesPerGame
                      ? Math.round(totals.minutesPerGame)
                      : "0"}
                  </span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Rating:</span>
                  <span
                    className={`stat-value ${getRatingColorClass(
                      totals.rating
                    )}`}
                  >
                    {totals.rating ? Number(totals.rating).toFixed(1) : "N/A"}
                  </span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Yellow Cards:</span>
                  <span className="stat-value">
                    {formatStat(displayStats.yellowCards, 0)}
                  </span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Red Cards:</span>
                  <span className="stat-value">
                    {formatStat(displayStats.redCards, 0)}
                  </span>
                </div>
              </div>
            </div>

            {isGoalkeeper ? (
              <>
                {/* Goalkeeping Stats */}
                <div className="stats-section goalkeeping-stats">
                  <h4 className="stats-section-title">Goalkeeping</h4>
                  <div className="stats-grid">
                    <div className="stat-item">
                      <span className="stat-label">Goals Conceded:</span>
                      <span className="stat-value">
                        {formatStat(displayStats.goalsConceded)}
                      </span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">Saves:</span>
                      <span className="stat-value">
                        {formatStat(displayStats.saves)}
                      </span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">Clean Sheets:</span>
                      <span className="stat-value">
                        {formatStat(displayStats.cleanSheets || 0)}
                      </span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">Save Percentage:</span>
                      <span className="stat-value">
                        {totals.saves && totals.goalsConceded
                          ? `${(
                              (totals.saves /
                                (totals.saves + totals.goalsConceded)) *
                              100
                            ).toFixed(1)}%`
                          : "N/A"}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Passing Stats for GK */}
                <div className="stats-section passing-stats">
                  <h4 className="stats-section-title">Passing</h4>
                  <div className="stats-grid">
                    <div className="stat-item">
                      <span className="stat-label">Total Passes:</span>
                      <span className="stat-value">
                        {formatStat(displayStats.passesTotal)}
                      </span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">Key Passes:</span>
                      <span className="stat-value">
                        {formatStat(displayStats.passesKey)}
                      </span>
                    </div>
                  </div>
                </div>
              </>
            ) : (
              <>
                {/* Order sections based on position */}
                {player.position?.toLowerCase().includes("def") ? (
                  <>
                    <DefendingStats
                      displayStats={displayStats}
                      formatStat={formatStat}
                    />
                    <PassingStats
                      displayStats={displayStats}
                      formatStat={formatStat}
                      totals={totals}
                    />
                    <AttackingStats
                      displayStats={displayStats}
                      formatStat={formatStat}
                      totals={totals}
                    />
                  </>
                ) : player.position?.toLowerCase().includes("mid") ? (
                  <>
                    <PassingStats
                      displayStats={displayStats}
                      formatStat={formatStat}
                      totals={totals}
                    />
                    <AttackingStats
                      displayStats={displayStats}
                      formatStat={formatStat}
                      totals={totals}
                    />
                    <DefendingStats
                      displayStats={displayStats}
                      formatStat={formatStat}
                    />
                  </>
                ) : (
                  <>
                    <AttackingStats
                      displayStats={displayStats}
                      formatStat={formatStat}
                      totals={totals}
                    />
                    <PassingStats
                      displayStats={displayStats}
                      formatStat={formatStat}
                      totals={totals}
                    />
                    <DefendingStats
                      displayStats={displayStats}
                      formatStat={formatStat}
                    />
                  </>
                )}
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

// Helper components for different stat sections
const DefendingStats = ({ displayStats, formatStat }) => (
  <div className="stats-section defending-stats">
    <h4 className="stats-section-title">Defending</h4>
    <div className="stats-grid">
      <div className="stat-item">
        <span className="stat-label">Tackles:</span>
        <span className="stat-value">
          {formatStat(displayStats.tacklesTotal)}
        </span>
      </div>
      <div className="stat-item">
        <span className="stat-label">Blocks:</span>
        <span className="stat-value">
          {formatStat(displayStats.tacklesBlocks)}
        </span>
      </div>
      <div className="stat-item">
        <span className="stat-label">Interceptions:</span>
        <span className="stat-value">
          {formatStat(displayStats.interceptions)}
        </span>
      </div>
      <div className="stat-item">
        <span className="stat-label">Fouls Committed:</span>
        <span className="stat-value">
          {formatStat(displayStats.foulsCommitted)}
        </span>
      </div>
    </div>
  </div>
);

const PassingStats = ({ displayStats, formatStat, totals }) => (
  <div className="stats-section passing-stats">
    <h4 className="stats-section-title">Passing</h4>
    <div className="stats-grid">
      <div className="stat-item">
        <span className="stat-label">Assists:</span>
        <span className="stat-value">{formatStat(displayStats.assists)}</span>
      </div>
      <div className="stat-item">
        <span className="stat-label">Total Passes:</span>
        <span className="stat-value">
          {formatStat(displayStats.passesTotal)}
        </span>
      </div>
      <div className="stat-item">
        <span className="stat-label">Key Passes:</span>
        <span className="stat-value">{formatStat(displayStats.passesKey)}</span>
      </div>
    </div>
  </div>
);

const AttackingStats = ({ displayStats, formatStat, totals }) => {
  const penaltySuccessRate =
    displayStats.penaltiesScored && displayStats.penaltiesScored > 0
      ? (displayStats.penaltiesScored /
          (displayStats.penaltiesScored +
            (displayStats.penaltiesMissed || 0))) *
        100
      : 0;

  // Calculate shot accuracy locally
  const shotAccuracy =
    totals.shotsTotal && totals.shotsTotal > 0 && totals.shotsOnTarget
      ? (totals.shotsOnTarget / totals.shotsTotal) * 100
      : 0;

  // Calculate dribble success rate locally
  const dribbleSuccessRate =
    totals.dribblesAttempts &&
    totals.dribblesAttempts > 0 &&
    totals.dribblesSuccess
      ? (totals.dribblesSuccess / totals.dribblesAttempts) * 100
      : 0;

  return (
    <div className="stats-section attacking-stats">
      <h4 className="stats-section-title">Attacking</h4>
      <div className="stats-grid">
        <div className="stat-item">
          <span className="stat-label">Goals:</span>
          <span className="stat-value">{formatStat(displayStats.goals)}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Total Shots:</span>
          <span className="stat-value">
            {formatStat(displayStats.shotsTotal)}
          </span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Shots on Target:</span>
          <span className="stat-value">
            {formatStat(displayStats.shotsOnTarget)}
          </span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Shot Accuracy:</span>
          <span className="stat-value">
            {totals.shotAccuracy ? `${totals.shotAccuracy.toFixed(1)}%` : "N/A"}
          </span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Dribble Attempts:</span>
          <span className="stat-value">
            {formatStat(displayStats.dribblesAttempts)}
          </span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Dribble Success:</span>
          <span className="stat-value">
            {formatStat(displayStats.dribblesSuccess)}
          </span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Dribble Success Rate:</span>
          <span className="stat-value">
            {totals.dribbleSuccessRate
              ? `${totals.dribbleSuccessRate.toFixed(1)}%`
              : "N/A"}
          </span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Fouls Drawn:</span>
          <span className="stat-value">
            {formatStat(displayStats.foulsDrawn)}
          </span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Penalties Won:</span>
          <span className="stat-value">
            {formatStat(displayStats.penaltiesWon)}
          </span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Penalties Scored:</span>
          <span className="stat-value">
            {formatStat(displayStats.penaltiesScored)}
          </span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Penalty Success Rate:</span>
          <span className="stat-value">
            {penaltySuccessRate > 0
              ? `${penaltySuccessRate.toFixed(1)}%`
              : "N/A"}
          </span>
        </div>
      </div>
    </div>
  );
};
const CommentsSection = ({ comments, playerId }) => {
  return (
    <div className="player-section">
      <div className="section-header">
        <h3 className="section-title">
          <span>💬</span>
          Recent Comments
        </h3>
        <button className="view-all-btn">View All</button>
      </div>
      <div className="section-content">
        {comments && comments.length > 0 ? (
          <div className="comments-list">
            {comments.slice(0, 5).map((comment, index) => (
              <div key={comment.id || index} className="comment-item">
                <div className="comment-header">
                  <div className="comment-author">
                    <div className="comment-avatar">
                      {comment.authorName?.charAt(0) || "?"}
                    </div>
                    <span className="comment-author-name">
                      {comment.authorName || "Anonymous"}
                    </span>
                  </div>
                  <span className="comment-date">
                    {new Date(comment.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <div className="comment-content">{comment.content}</div>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <div className="empty-state-icon">💬</div>
            <h4 className="empty-state-title">No Comments Yet</h4>
            <p className="empty-state-message">
              Be the first to share your thoughts about this player.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

const RatingsSection = ({ ratings }) => {
  const averageRating =
    ratings && ratings.length > 0
      ? ratings.reduce((sum, rating) => sum + rating.rating, 0) / ratings.length
      : 0;

  return (
    <div className="player-section">
      <div className="section-header">
        <h3 className="section-title">
          <span>⭐</span>
          Community Ratings
        </h3>
        <button className="view-all-btn">View All</button>
      </div>
      <div className="section-content">
        <div className="ratings-summary">
          <div className="average-rating">
            <span
              className={`rating-score ${getRatingColorClass(averageRating)}`}
            >
              {averageRating.toFixed(1)}
            </span>
            <span className="rating-total">/ 10</span>
          </div>
          <div className="ratings-count">
            Based on {ratings?.length || 0} ratings
          </div>
        </div>

        {ratings && ratings.length > 0 ? (
          <div className="recent-ratings">
            {ratings.slice(0, 3).map((rating, index) => (
              <div key={rating.id || index} className="rating-item">
                <div className="rating-header">
                  <span className="rating-author">{rating.authorName}</span>
                  <span
                    className={`rating-value ${getRatingColorClass(
                      rating.rating
                    )}`}
                  >
                    {rating.rating}/10
                  </span>
                </div>
                {rating.notes && (
                  <div className="rating-notes">{rating.notes}</div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <div className="empty-state-icon">⭐</div>
            <h4 className="empty-state-title">No Ratings Yet</h4>
            <p className="empty-state-message">
              Be the first to rate this player's performance.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

// Scouting Reports Section
const ScoutingReportsSection = ({ playerId }) => {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadReports = async () => {
      try {
        const scoutingReports = await ApiService.getPlayerScoutingReports(
          playerId,
          0,
          5
        );
        setReports(scoutingReports.content || []);
      } catch (error) {
        console.error("Error loading scouting reports:", error);
        setReports([]);
      } finally {
        setLoading(false);
      }
    };

    loadReports();
  }, [playerId]);

  return (
    <div className="player-section">
      <div className="section-header">
        <h3 className="section-title">
          <span>📋</span>
          Scouting Reports
        </h3>
        <button className="view-all-btn">View All</button>
      </div>
      <div className="section-content">
        {loading ? (
          <div className="loading-state">Loading reports...</div>
        ) : reports && reports.length > 0 ? (
          <div className="scouting-reports-list">
            {reports.map((report, index) => (
              <div key={report.id || index} className="report-item">
                <div className="report-header">
                  <h4 className="report-title">{report.title}</h4>
                  <span
                    className={`report-rating ${getRatingColorClass(
                      report.overallRating
                    )}`}
                  >
                    {report.overallRating}/10
                  </span>
                </div>
                <div className="report-meta">
                  <span className="report-author">By {report.scoutName}</span>
                  <span className="report-date">
                    {new Date(report.createdAt).toLocaleDateString()}
                  </span>
                </div>
                {report.summary && (
                  <div className="report-summary">{report.summary}</div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <div className="empty-state-icon">📋</div>
            <h4 className="empty-state-title">No Scouting Reports</h4>
            <p className="empty-state-message">
              No detailed scouting reports have been created for this player
              yet.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

// Loading Skeleton
const LoadingSkeleton = () => {
  return (
    <div className="loading-skeleton">
      <div className="skeleton-header">
        <div className="skeleton-photo"></div>
        <div className="skeleton-info">
          <div className="skeleton-text long"></div>
          <div className="skeleton-text medium"></div>
          <div className="skeleton-text short"></div>
        </div>
      </div>
      <div className="skeleton-sections">
        {[1, 2, 3, 4, 5].map((i) => (
          <div key={i} className="skeleton-section">
            <div className="skeleton-text medium"></div>
            <div className="skeleton-text long"></div>
            <div className="skeleton-text short"></div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PlayerPage;
