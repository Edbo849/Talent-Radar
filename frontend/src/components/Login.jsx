import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import "./static/Auth.css";

/**
 * Login component for user authentication.
 * Handles form state, validation, and login logic.
 *
 * @param {Function} onToggleMode - Callback to switch to signup mode.
 * @param {Function} onSuccess - Callback after successful login.
 */
const Login = ({ onToggleMode, onSuccess }) => {
  // Initialising states
  const [formData, setFormData] = useState({
    usernameOrEmail: "",
    password: "",
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  // Get login function from AuthContext
  const { login } = useAuth();

  /**
   * Handles input changes and clears field-specific errors.
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }
  };

  /**
   * Validates the form fields.
   * @returns {boolean} True if form is valid, false otherwise.
   */
  const validateForm = () => {
    const newErrors = {};

    if (!formData.usernameOrEmail.trim()) {
      newErrors.usernameOrEmail = "Username or email is required";
    }

    if (!formData.password) {
      newErrors.password = "Password is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * Handles form submission and login logic.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    try {
      await login(formData);
      // Call onSuccess callback if provided
      if (onSuccess) onSuccess();
    } catch (error) {
      setErrors({
        submit: "Login failed. Please check your credentials.",
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container flex items-center justify-center bg-navy text-white min-h-full">
      <div className="auth-card bg-white rounded-2xl shadow-xl max-w-lg w-full p-3xl text-center">
        <img
          src="/talent_radar.png"
          alt="Talent Radar Logo"
          className="mb-md mx-auto d-block"
          style={{ width: 120, height: 120, borderRadius: "10%" }}
        />
        <div className="auth-header text-center mb-xl">
          <h2 className="text-navy font-heading text-3xl mb-sm font-bold">
            🎯 Welcome Back
          </h2>
          <p className="text-gray text-base m-0">
            Sign in to your Talent Radar account
          </p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="auth-form flex flex-col gap-lg"
        >
          <div className="form-group flex flex-col">
            <label
              htmlFor="usernameOrEmail"
              className="text-navy font-semibold mb-sm text-sm"
            >
              Username or Email
            </label>
            <input
              type="text"
              id="usernameOrEmail"
              name="usernameOrEmail"
              value={formData.usernameOrEmail}
              onChange={handleChange}
              className={`p-md border-2 border-gray-200 rounded-lg text-base font-primary bg-white text-navy${
                errors.usernameOrEmail ? " error" : ""
              }`}
              placeholder="Enter your username or email"
              disabled={isLoading}
              autoComplete="username"
            />
            {errors.usernameOrEmail && (
              <span className="error-message text-error text-sm mt-xs d-block">
                {errors.usernameOrEmail}
              </span>
            )}
          </div>

          <div className="form-group flex flex-col">
            <label
              htmlFor="password"
              className="text-navy font-semibold mb-sm text-sm"
            >
              Password
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className={`p-md border-2 border-gray-200 rounded-lg text-base font-primary bg-white text-navy${
                errors.usernameOrEmail ? " error" : ""
              }`}
              placeholder="Enter your password"
              disabled={isLoading}
              autoComplete="current-password"
            />
            {errors.password && (
              <span className="error-message text-error text-sm mt-xs d-block">
                {errors.password}
              </span>
            )}
          </div>

          {errors.submit && (
            <div
              className="error-message submit-error text-error border-error rounded-md p-md mb-md text-center"
              style={{
                backgroundColor: "rgba(218, 51, 51, 0.41)",
                border: "1px solid #da2929ff",
              }}
            >
              {errors.submit}
            </div>
          )}

          <button
            type="submit"
            className="auth-button bg-navy text-white rounded-lg font-semibold p-lg px-xl mt-md w-full transition-base"
            disabled={isLoading}
          >
            {isLoading ? "Signing In..." : "Sign In"}
          </button>
        </form>

        <div className="auth-footer text-center mt-xl pt-lg border-t border-gray-200">
          <p className="text-gray m-0 text-sm">
            Don't have an account?{" "}
            <button
              type="button"
              onClick={onToggleMode}
              className="toggle-button text-accent font-semibold underline cursor-pointer transition-fast"
            >
              Register
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
