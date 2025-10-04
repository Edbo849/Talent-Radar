// Base URL for all API requests
const API_BASE_URL = "http://localhost:8080/api";

// ApiService class handles all authentication and user-related API calls
class ApiService {
  /**
   * Logs in a user with the provided credentials.
   * @param {Object} credentials - The user's login credentials.
   * @returns {Promise<Object>} - The response containing user and token.
   * @throws {Error} - If login fails.
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
   * @param {Object} userData - The user's registration data.
   * @returns {Promise<Object>} - The response containing user and token.
   * @throws {Error} - If registration fails.
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
   * @param {string} token - The JWT token for authentication.
   * @returns {Promise<Object>} - The current user's data.
   * @throws {Error} - If fetching user data fails.
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
}

// Export a singleton instance of ApiService
export default new ApiService();
