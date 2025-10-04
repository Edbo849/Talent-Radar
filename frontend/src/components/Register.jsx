import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import "./static/Auth.css";
import "../utilities.css";

/**
 * Register component for user signup.
 * Handles form state, validation, and registration logic.
 *
 * @param {Function} onToggleMode - Callback to switch to login mode.
 * @param {Function} onSuccess - Callback after successful registration.
 */
const Register = ({ onToggleMode, onSuccess }) => {
  // Set state fields
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    firstName: "",
    lastName: "",
    role: "USER",
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const { register } = useAuth();

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

    // Username validation
    if (!formData.username.trim()) {
      newErrors.username = "Username is required";
    } else if (formData.username.length < 3 || formData.username.length > 50) {
      newErrors.username = "Username must be between 3 and 50 characters";
    }

    // Email validation
    if (!formData.email.trim()) {
      newErrors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Please enter a valid email address";
    }

    // Password validation
    if (!formData.password) {
      newErrors.password = "Password is required";
    } else if (formData.password.length < 6 || formData.password.length > 100) {
      newErrors.password = "Password must be between 6 and 100 characters";
    }

    // Confirm password validation
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = "Please confirm your password";
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = "Passwords do not match";
    }

    // Optional field validations
    if (formData.firstName && formData.firstName.length > 50) {
      newErrors.firstName = "First name must not exceed 50 characters";
    }

    if (formData.lastName && formData.lastName.length > 50) {
      newErrors.lastName = "Last name must not exceed 50 characters";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * Handles form submission and registration logic.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validate form before submitting
    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    try {
      // Remove confirmPassword before sending to API
      const { confirmPassword, ...registrationData } = formData;
      await register(registrationData);
      // Call onSuccess callback if provided
      onSuccess && onSuccess();
    } catch (error) {
      setErrors({
        submit: error.message || "Registration failed. Please try again.",
      });
    } finally {
      setIsLoading(false);
    }
  };

  // Render registration form
  return (
    <div className="auth-container flex items-center justify-center bg-navy text-white min-h-full">
      <div className="auth-card bg-white rounded-2xl shadow-xl max-w-lg w-full p-3xl">
        <div className="auth-header text-center mb-2xl">
          <h2 className="text-navy font-heading text-3xl mb-sm font-bold">
            🎯 Join Talent Radar
          </h2>
          <p className="text-gray text-base m-0">Create your account</p>
        </div>
        <form
          onSubmit={handleSubmit}
          className="auth-form flex flex-col gap-lg"
        >
          <div className="form-row grid grid-cols-2 gap-md">
            <div className="form-group flex flex-col">
              <label
                htmlFor="firstName"
                className="text-navy font-semibold mb-sm text-sm"
              >
                First Name
              </label>
              <input
                type="text"
                id="firstName"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                className={`p-md border-2 border-gray-200 rounded-lg text-base font-primary bg-white text-navy ${
                  errors.firstName ? "border-error bg-error" : ""
                }`}
                placeholder="Your first name"
                disabled={isLoading}
              />
              {errors.firstName && (
                <span className="error-message text-error text-sm mt-xs d-block">
                  {errors.firstName}
                </span>
              )}
            </div>
            <div className="form-group flex flex-col">
              <label
                htmlFor="lastName"
                className="text-navy font-semibold mb-sm text-sm"
              >
                Last Name
              </label>
              <input
                type="text"
                id="lastName"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                className={`p-md border-2 border-gray-200 rounded-lg text-base font-primary bg-white text-navy ${
                  errors.lastName ? "border-error bg-error" : ""
                }`}
                placeholder="Your last name"
                disabled={isLoading}
              />
              {errors.lastName && (
                <span className="error-message text-error text-sm mt-xs d-block">
                  {errors.lastName}
                </span>
              )}
            </div>
          </div>
          <div className="form-group flex flex-col">
            <label
              htmlFor="username"
              className="text-navy font-semibold mb-sm text-sm"
            >
              Username *
            </label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              className={`p-md border-2 border-gray-200 rounded-lg text-base font-primary bg-white text-navy ${
                errors.username ? "border-error bg-error" : ""
              }`}
              placeholder="Choose a username (3-50 characters)"
              disabled={isLoading}
              required
            />
            {errors.username && (
              <span className="error-message text-error text-sm mt-xs d-block">
                {errors.username}
              </span>
            )}
          </div>
          <div className="form-group flex flex-col">
            <label
              htmlFor="email"
              className="text-navy font-semibold mb-sm text-sm"
            >
              Email Address *
            </label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className={`p-md border-2 border-gray-200 rounded-lg text-base font-primary bg-white text-navy ${
                errors.email ? "border-error bg-error" : ""
              }`}
              placeholder="your.email@example.com"
              disabled={isLoading}
              required
            />
            {errors.email && (
              <span className="error-message text-error text-sm mt-xs d-block">
                {errors.email}
              </span>
            )}
          </div>
          <div className="form-group flex flex-col">
            <label
              htmlFor="role"
              className="text-navy font-semibold mb-sm text-sm"
            >
              Account Type
            </label>
            <select
              id="role"
              name="role"
              value={formData.role}
              onChange={handleChange}
              className="p-md border-2 border-gray-200 rounded-lg text-base font-primary bg-white text-navy"
              disabled={isLoading}
            >
              <option value="USER">Fan/Enthusiast</option>
              <option value="SCOUT">Scout</option>
              <option value="COACH">Coach</option>
            </select>
          </div>
          <div className="form-row grid grid-cols-2 gap-md">
            <div className="form-group flex flex-col">
              <label
                htmlFor="password"
                className="text-navy font-semibold mb-sm text-sm"
              >
                Password *
              </label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className={`p-md border-2 border-gray-200 rounded-lg text-base font-primary bg-white text-navy ${
                  errors.password ? "border-error bg-error" : ""
                }`}
                placeholder="At least 6 characters"
                disabled={isLoading}
                required
              />
              {errors.password && (
                <span className="error-message text-error text-sm mt-xs d-block">
                  {errors.password}
                </span>
              )}
            </div>
            <div className="form-group flex flex-col">
              <label
                htmlFor="confirmPassword"
                className="text-navy font-semibold mb-sm text-sm"
              >
                Confirm Password *
              </label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className={`p-md border-2 border-gray-200 rounded-lg text-base font-primary bg-white text-navy ${
                  errors.confirmPassword ? "border-error bg-error" : ""
                }`}
                placeholder="Confirm your password"
                disabled={isLoading}
                required
              />
              {errors.confirmPassword && (
                <span className="error-message text-error text-sm mt-xs d-block">
                  {errors.confirmPassword}
                </span>
              )}
            </div>
          </div>
          {errors.submit && (
            <div className="error-message submit-error text-error bg-error border-error rounded-md p-md mb-md text-center">
              {errors.submit}
            </div>
          )}
          <button
            type="submit"
            className="auth-button bg-navy text-white rounded-lg font-semibold p-lg px-xl mt-md w-full transition-base"
            disabled={isLoading}
          >
            {isLoading ? "Creating Account..." : "Create Account"}
          </button>
        </form>
        <div className="auth-footer text-center mt-xl pt-lg border-t border-gray-200">
          <p className="text-gray m-0 text-sm">
            Already have an account?{" "}
            <button
              type="button"
              onClick={onToggleMode}
              className="toggle-button text-accent font-semibold underline cursor-pointer transition-fast"
            >
              Sign In
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
