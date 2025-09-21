package com.talentradar.dto.user;

/**
 * Data Transfer Object for login response. Contains authentication token and
 * user information.
 */
public class UserLoginResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    private UserDTO user;
    private Long expiresIn; // Token expiration time in seconds

    // Constructors
    public UserLoginResponseDTO() {
    }

    public UserLoginResponseDTO(String token, UserDTO user, Long expiresIn) {
        this.token = token;
        this.user = user;
        this.expiresIn = expiresIn;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
