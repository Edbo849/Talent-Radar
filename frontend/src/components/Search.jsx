import React, { useState, useEffect, useCallback } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import Navbar from "./Navbar";
import ApiService from "../services/api";
import FlagService from "../services/flagService";
import "./static/Search.css";

const CountryFlag = ({ nationality, className = "nationality-flag" }) => {
  const [flag, setFlag] = useState(null);

  useEffect(() => {
    const loadFlag = async () => {
      if (!nationality) return;
      try {
        await FlagService.loadCountries();
        const flagData = FlagService.getFlag(nationality);
        setFlag(flagData);
      } catch (error) {
        console.error("Error loading flag:", error);
      }
    };
    loadFlag();
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

const SearchPage = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [searchState, setSearchState] = useState({
    query: searchParams.get("q") || "",
    players: [],
    loading: false,
    error: null,
    page: parseInt(searchParams.get("page")) || 0,
    totalPages: 0,
    totalResults: 0,
  });

  const [filters, setFilters] = useState({
    position: searchParams.get("position") || "",
    nationality: searchParams.get("nationality") || "",
    minAge: searchParams.get("minAge") || "",
    maxAge: searchParams.get("maxAge") || "",
    league: searchParams.get("league") || "",
  });

  const [filterOptions, setFilterOptions] = useState({
    positions: [
      { value: "Goalkeeper", label: "Goalkeeper" },
      { value: "Defender", label: "Defender" },
      { value: "Midfielder", label: "Midfielder" },
      { value: "Attacker", label: "Attacker" },
    ],
    nationalities: [],
    leagues: [],
  });

  // Load filter options
  useEffect(() => {
    const loadFilterOptions = async () => {
      try {
        const [countries, leagues] = await Promise.all([
          ApiService.getAllCountries(),
          ApiService.getAllLeagues(),
        ]);

        // Keep full league objects with country information
        const leaguesWithCountry = (leagues.content || leagues)
          .filter((league) => league.name) // Filter out any leagues without names
          .sort((a, b) => a.name.localeCompare(b.name));

        setFilterOptions((prev) => ({
          ...prev,
          nationalities: countries.map((c) => c.name).sort(),
          leagues: leaguesWithCountry, // Store full league objects
        }));
      } catch (error) {
        console.error("Error loading filter options:", error);
      }
    };

    loadFilterOptions();
  }, []);

  // Sync state from URL params (runs when URL changes)
  useEffect(() => {
    const queryFromUrl = searchParams.get("q") || "";
    const pageFromUrl = parseInt(searchParams.get("page")) || 0;
    const positionFromUrl = searchParams.get("position") || "";
    const nationalityFromUrl = searchParams.get("nationality") || "";
    const minAgeFromUrl = searchParams.get("minAge") || "";
    const maxAgeFromUrl = searchParams.get("maxAge") || "";
    const leagueFromUrl = searchParams.get("league") || "";

    setSearchState((prev) => ({
      ...prev,
      query: queryFromUrl,
      page: pageFromUrl,
    }));

    setFilters({
      position: positionFromUrl,
      nationality: nationalityFromUrl,
      minAge: minAgeFromUrl,
      maxAge: maxAgeFromUrl,
      league: leagueFromUrl,
    });
  }, [searchParams]); // Only depend on searchParams

  // Perform search
  const performSearch = useCallback(async () => {
    const queryToSearch = searchState.query.trim();
    if (!searchState.query.trim()) return;

    setSearchState((prev) => ({ ...prev, loading: true, error: null }));

    try {
      const params = {
        query: queryToSearch,
        page: searchState.page,
        size: 9,
      };

      // Only add filters if they have values
      if (filters.position) params.position = filters.position;
      if (filters.nationality) params.nationality = filters.nationality;
      if (filters.league) params.league = filters.league;
      if (filters.minAge) params.minAge = parseInt(filters.minAge);
      if (filters.maxAge) params.maxAge = parseInt(filters.maxAge);

      console.log("Searching with params:", params);

      const response = await ApiService.searchPlayers(params);

      setSearchState((prev) => ({
        ...prev,
        players: response.content || [],
        totalPages: response.totalPages || 0,
        totalResults: response.totalElements || 0,
        loading: false,
      }));
    } catch (error) {
      console.error("Search error:", error);
      setSearchState((prev) => ({
        ...prev,
        error: "Failed to search players. Please try again.",
        loading: false,
      }));
    }
  }, [searchState.query, searchState.page, filters]);

  // Trigger search when query or filters change
  useEffect(() => {
    if (searchState.query) {
      performSearch();
    }
  }, [searchState.query, searchState.page, filters, performSearch]);

  const handleFilterChange = (filterName, value) => {
    const newFilters = { ...filters, [filterName]: value };

    // Age validation
    if (filterName === "minAge") {
      const minAge = parseInt(value) || 0;
      const maxAge = parseInt(filters.maxAge) || 999;
      if (minAge > maxAge && filters.maxAge) {
        // If min exceeds max, update max to match min
        newFilters.maxAge = value;
      }
    } else if (filterName === "maxAge") {
      const maxAge = parseInt(value) || 999;
      const minAge = parseInt(filters.minAge) || 0;
      if (maxAge < minAge && filters.minAge) {
        // If max is less than min, update min to match max
        newFilters.minAge = value;
      }
    }

    setFilters(newFilters);

    // Update URL
    const params = new URLSearchParams();
    if (searchState.query) params.set("q", searchState.query);
    params.set("page", "0"); // Reset to first page

    Object.entries(newFilters).forEach(([key, val]) => {
      if (val) params.set(key, val);
    });

    setSearchParams(params, { replace: true });
  };

  const clearFilters = () => {
    const clearedFilters = {
      position: "",
      nationality: "",
      minAge: "",
      maxAge: "",
      league: "",
    };
    setFilters(clearedFilters);

    // Update URL
    const params = new URLSearchParams();
    if (searchState.query) params.set("q", searchState.query);
    params.set("page", "0");

    setSearchParams(params, { replace: true });
  };

  const handlePlayerClick = (playerId) => {
    navigate(`/player/${playerId}`);
  };

  const handlePageChange = (newPage) => {
    // Update URL
    const params = new URLSearchParams();
    if (searchState.query) params.set("q", searchState.query);
    params.set("page", newPage.toString());

    Object.entries(filters).forEach(([key, value]) => {
      if (value) params.set(key, value);
    });

    setSearchParams(params, { replace: true });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const activeFiltersCount = Object.values(filters).filter(Boolean).length;

  return (
    <div className="search-page-container">
      <Navbar />
      <div className="search-page-content">
        <div className="search-header">
          <h1 className="search-title">
            Search Results
            {searchState.query && (
              <span className="search-query"> for "{searchState.query}"</span>
            )}
          </h1>
          {searchState.totalResults > 0 && (
            <p className="search-results-count">
              Found {searchState.totalResults} player
              {searchState.totalResults !== 1 ? "s" : ""}
            </p>
          )}
        </div>

        <div className="search-layout">
          {/* Filters Sidebar */}
          <aside className="search-filters">
            <div className="filters-header">
              <h3>Filters</h3>
              {activeFiltersCount > 0 && (
                <button onClick={clearFilters} className="clear-filters-btn">
                  Clear All ({activeFiltersCount})
                </button>
              )}
            </div>

            <div className="filter-group">
              <label className="filter-label">Position</label>
              <select
                value={filters.position}
                onChange={(e) => handleFilterChange("position", e.target.value)}
                className="filter-select"
              >
                <option value="">All Positions</option>
                {filterOptions.positions.map((pos) => (
                  <option key={pos.value} value={pos.value}>
                    {pos.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-group">
              <label className="filter-label">League</label>
              <select
                value={filters.league}
                onChange={(e) => handleFilterChange("league", e.target.value)}
                className="filter-select"
              >
                <option value="">All Leagues</option>
                {filterOptions.leagues.map((league, index) => (
                  <option
                    key={`league-${league.id || index}`}
                    value={league.name}
                  >
                    {league.name}
                    {league.country &&
                      league.country.name &&
                      ` - ${league.country.name}`}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-group">
              <label className="filter-label">Nationality</label>
              <select
                value={filters.nationality}
                onChange={(e) =>
                  handleFilterChange("nationality", e.target.value)
                }
                className="filter-select"
              >
                <option value="">All Nationalities</option>
                {filterOptions.nationalities.map((nat) => (
                  <option key={nat} value={nat}>
                    {nat}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-group">
              <label className="filter-label">Age Range</label>
              <div className="age-range-inputs">
                <input
                  type="number"
                  placeholder="Min"
                  value={filters.minAge}
                  onChange={(e) => handleFilterChange("minAge", e.target.value)}
                  className="filter-input"
                  min="16"
                  max={filters.maxAge || "45"}
                />
                <span className="age-separator">-</span>
                <input
                  type="number"
                  placeholder="Max"
                  value={filters.maxAge}
                  onChange={(e) => handleFilterChange("maxAge", e.target.value)}
                  className="filter-input"
                  min={filters.minAge || "16"}
                  max="45"
                />
              </div>
              {(filters.minAge || filters.maxAge) && (
                <p className="filter-hint">
                  {filters.minAge && filters.maxAge
                    ? `Ages ${filters.minAge} to ${filters.maxAge}`
                    : filters.minAge
                    ? `Ages ${filters.minAge}+`
                    : `Ages up to ${filters.maxAge}`}
                </p>
              )}
            </div>
          </aside>

          {/* Results Area */}
          <main className="search-results">
            {searchState.loading ? (
              <div className="loading-state">
                <div className="loading-spinner"></div>
                <p>Searching players...</p>
              </div>
            ) : searchState.error ? (
              <div className="error-state">
                <div className="error-icon">⚠️</div>
                <h3>Error</h3>
                <p>{searchState.error}</p>
                <button onClick={performSearch} className="retry-btn">
                  Try Again
                </button>
              </div>
            ) : searchState.players.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">🔍</div>
                <h3>No players found</h3>
                <p>
                  Try adjusting your search query or filters to find more
                  results.
                </p>
                {activeFiltersCount > 0 && (
                  <button onClick={clearFilters} className="retry-btn">
                    Clear Filters
                  </button>
                )}
              </div>
            ) : (
              <>
                <div className="players-grid">
                  {searchState.players.map((player) => (
                    <div
                      key={player.id}
                      className="player-card"
                      onClick={() => handlePlayerClick(player.id)}
                    >
                      <div className="player-card-avatar">
                        {player.photoUrl ? (
                          <img src={player.photoUrl} alt={player.name} />
                        ) : (
                          <div className="avatar-placeholder">
                            {player.name.charAt(0)}
                          </div>
                        )}
                      </div>
                      <div className="player-card-info">
                        <h4 className="player-card-name">{player.name}</h4>
                        <div className="player-card-details">
                          {player.nationality && (
                            <div className="player-card-nationality">
                              <CountryFlag nationality={player.nationality} />
                              <span>{player.nationality}</span>
                            </div>
                          )}
                          <div className="player-card-meta">
                            <span className="player-position">
                              {player.position}
                            </span>
                            <span className="player-age">
                              {player.age} years
                            </span>
                          </div>
                          {player.currentClubName && (
                            <div className="player-card-club">
                              {player.currentClubLogoUrl && (
                                <img
                                  src={player.currentClubLogoUrl}
                                  alt={player.currentClubName}
                                  className="club-logo-small"
                                />
                              )}
                              <span>{player.currentClubName}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Pagination */}
                {searchState.totalPages > 1 && (
                  <div className="pagination">
                    <button
                      onClick={() => handlePageChange(searchState.page - 1)}
                      disabled={searchState.page === 0}
                      className="pagination-btn"
                    >
                      ← Previous
                    </button>

                    <div className="pagination-pages">
                      {[...Array(searchState.totalPages)].map((_, index) => {
                        const showPage =
                          index === 0 ||
                          index === searchState.totalPages - 1 ||
                          Math.abs(index - searchState.page) <= 2;

                        const showEllipsis =
                          (index === 1 && searchState.page > 3) ||
                          (index === searchState.totalPages - 2 &&
                            searchState.page < searchState.totalPages - 4);

                        if (showEllipsis) {
                          return (
                            <span key={index} className="pagination-ellipsis">
                              ...
                            </span>
                          );
                        }

                        if (!showPage) return null;

                        return (
                          <button
                            key={index}
                            onClick={() => handlePageChange(index)}
                            className={`pagination-page ${
                              index === searchState.page ? "active" : ""
                            }`}
                          >
                            {index + 1}
                          </button>
                        );
                      })}
                    </div>

                    <button
                      onClick={() => handlePageChange(searchState.page + 1)}
                      disabled={searchState.page >= searchState.totalPages - 1}
                      className="pagination-btn"
                    >
                      Next →
                    </button>
                  </div>
                )}
              </>
            )}
          </main>
        </div>
      </div>
    </div>
  );
};

export default SearchPage;
