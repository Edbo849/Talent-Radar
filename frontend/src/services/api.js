// Base URL for all API requests
const API_BASE_URL = "http://localhost:8080/api";

// ApiService class handles all authentication and user-related API calls
class ApiService {
  /**
   * Logs in a user with the provided credentials.
   */
  async login(credentials) {
    const response = await fetch(`${API_BASE_URL}/users/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || "Login failed");
    }

    return response.json();
  }

  /**
   * Registers a new user.
   */
  async register(userData) {
    const response = await fetch(`${API_BASE_URL}/users/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || "Registration failed");
    }

    return response.json();
  }

  /**
   * Fetches the current logged-in user's data using a JWT token.
   */
  async getCurrentUser(token) {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch user data");
    }

    return response.json();
  }

  // Dashboard methods

  /**
   * Makes a general request to the API
   */
  async request(endpoint, method = "GET", body = null, token = null) {
    const url = `${API_BASE_URL}${endpoint}`;
    const config = {
      method,
      headers: {
        "Content-Type": "application/json",
      },
    };

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    if (body) {
      config.body = JSON.stringify(body);
    }

    const response = await fetch(url, config);

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || `HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  async getTrendingPlayers(limit = 25) {
    const response = await this.request(
      `/players/trending?limit=${limit}`,
      "GET"
    );
    return response;
  }

  async getTopRatedPlayers(limit = 5) {
    const response = await this.request(
      `/players/top-rated?limit=${limit}`,
      "GET"
    );
    console.log(response);
    return response;
  }

  async getTopRatedPlayersSeason(limit = 25) {
    const response = await this.request(
      `/players/top-rated-season?limit=${limit}`,
      "GET"
    );
    return response;
  }

  async getRecentPlayers(limit = 5) {
    const response = await this.request(
      `/players/recent?limit=${limit}`,
      "GET"
    );
    return response;
  }

  async getTopPlayersByStats(statistic, season = 2024, limit = 5) {
    const response = await this.request(
      `/players/top-by-stats?statistic=${statistic}&season=${season}&limit=${limit}`,
      "GET"
    );
    return response;
  }

  async getLatestReports(limit = 5) {
    const response = await this.request(
      `/scouting-reports/public/latest?limit=${limit}`,
      "GET"
    );
    return response;
  }

  async getMyActivity(token) {
    const response = await this.request(
      "/users/my-activity",
      "GET",
      null,
      token
    );
    return response;
  }

  async getScoutStats(token) {
    const response = await this.request(
      "/scouting-reports/my-stats",
      "GET",
      null,
      token
    );
    return response;
  }

  async getAdminStats(token) {
    const response = await this.request(
      "/users/admin-stats",
      "GET",
      null,
      token
    );
    return response;
  }

  async getPlatformStats(token) {
    const response = await this.request(
      "/users/platform-stats",
      "GET",
      null,
      token
    );
    return response;
  }

  async getMostActiveDiscussions(limit = 5) {
    const response = await this.request(
      `/discussions/threads/most-active?limit=${limit}`,
      "GET"
    );
    return response;
  }

  async getActivePolls(limit = 3) {
    const response = await this.request(`/polls/active?limit=${limit}`, "GET");
    return response;
  }

  async getAllCountries() {
    const response = await this.request("/countries", "GET");
    return response;
  }

  async getCountryFlag(countryName) {
    const response = await this.request(
      `/countries/flag/${encodeURIComponent(countryName)}`,
      "GET"
    );
    return response;
  }

  async getPlayer(playerId) {
    const response = await this.request(`/players/${playerId}`, "GET");
    return response;
  }

  async getPlayerStatistics(playerId) {
    const response = await this.request(
      `/players/${playerId}/statistics`,
      "GET"
    );
    return response;
  }

  async getPlayerTransfers(playerId) {
    const response = await this.request(
      `/players/${playerId}/transfers`,
      "GET"
    );
    return response;
  }

  async getPlayerInjuries(playerId) {
    const response = await this.request(`/players/${playerId}/injuries`, "GET");
    return response;
  }

  async getPlayerTrophies(playerId) {
    const response = await this.request(`/players/${playerId}/trophies`, "GET");
    return response;
  }

  async getPlayerComments(playerId, page = 0, size = 10) {
    const response = await this.request(
      `/players/${playerId}/comments?page=${page}&size=${size}`,
      "GET"
    );
    return response;
  }

  async getPlayerRatings(playerId, page = 0, size = 10) {
    const response = await this.request(
      `/players/${playerId}/ratings?page=${page}&size=${size}`,
      "GET"
    );
    return response;
  }

  async getPlayerScoutingReports(playerId, page = 0, size = 10) {
    const response = await this.request(
      `/scouting-reports/player/${playerId}?page=${page}&size=${size}`,
      "GET"
    );
    return response;
  }

  async getPlayerSidelined(playerId) {
    const response = await this.request(
      `/players/${playerId}/sidelined`,
      "GET"
    );
    return response;
  }
}

// Export a singleton instance of ApiService
export default new ApiService();
