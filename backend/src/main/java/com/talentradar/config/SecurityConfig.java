package com.talentradar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/health", "/status").permitAll()
                .requestMatchers("/data/**").permitAll()
                .requestMatchers("/admin/scheduled-tasks/**").permitAll()
                // Player-related endpoints
                .requestMatchers("/api/players").permitAll() // List players
                .requestMatchers("/api/players/*/view").permitAll() // Track player views
                .requestMatchers("/api/players/*/views/**").permitAll() // View operations
                .requestMatchers("/api/players/*/comments").permitAll() // Read/create comments
                .requestMatchers("/api/players/*/comments/**").permitAll() // Comment operations
                .requestMatchers("/api/players/*/ratings").permitAll() // Read/create ratings
                .requestMatchers("/api/players/*/ratings/**").permitAll() // Rating operations
                .requestMatchers("/api/players/trending").permitAll() // Trending players
                .requestMatchers("/api/players/u21").permitAll() // U21 players
                .requestMatchers("/api/players/search").permitAll() // Search players
                .requestMatchers("/api/players/**").permitAll() // Individual player details

                // Club endpoints - MISSING
                .requestMatchers("/api/clubs").permitAll() // List clubs
                .requestMatchers("/api/clubs/*/players").permitAll() // Club players
                .requestMatchers("/api/clubs/count").permitAll() // Club count
                .requestMatchers("/api/clubs/countries").permitAll() // Club countries
                .requestMatchers("/api/clubs/search").permitAll() // Search clubs
                .requestMatchers("/api/clubs/**").permitAll() // Individual club details

                // League endpoints - MISSING
                .requestMatchers("/api/leagues").permitAll() // List leagues
                .requestMatchers("/api/leagues/external/**").permitAll() // External league lookup
                .requestMatchers("/api/leagues/season/**").permitAll() // Leagues by season
                .requestMatchers("/api/leagues/country/**").permitAll() // Leagues by country
                .requestMatchers("/api/leagues/type/**").permitAll() // Leagues by type
                .requestMatchers("/api/leagues/search").permitAll() // Search leagues
                .requestMatchers("/api/leagues/top").permitAll() // Top leagues
                .requestMatchers("/api/leagues/current-season").permitAll() // Current season
                .requestMatchers("/api/leagues/*/clubs").permitAll() // League clubs
                .requestMatchers("/api/leagues/types").permitAll() // League types
                .requestMatchers("/api/leagues/seasons").permitAll() // Available seasons
                .requestMatchers("/api/leagues/count").permitAll() // League count
                .requestMatchers("/api/leagues/**").permitAll() // Individual league details

                // Discussion endpoints
                .requestMatchers("/api/discussions/categories").permitAll() // List categories
                .requestMatchers("/api/discussions/categories/**").permitAll() // Category operations
                .requestMatchers("/api/discussions/threads").permitAll() // Read threads
                .requestMatchers("/api/discussions/threads/**").permitAll() // Thread operations
                .requestMatchers("/api/discussions/replies/**").permitAll() // Reply operations
                .requestMatchers("/api/discussions/search").permitAll() // Search discussions

                // Poll endpoints
                .requestMatchers("/api/polls").permitAll() // List polls
                .requestMatchers("/api/polls/**").permitAll() // Poll operations

                // User endpoints
                .requestMatchers("/api/users/register").permitAll() // User registration
                .requestMatchers("/api/users/login").permitAll() // User login
                .requestMatchers("/api/users/me").permitAll() // Current user profile
                .requestMatchers("/api/users/*/profile").permitAll() // View user profiles
                .requestMatchers("/api/users/*/follow").permitAll() // Follow operations
                .requestMatchers("/api/users/*/unfollow").permitAll() // Unfollow operations
                .requestMatchers("/api/users/*/followers").permitAll() // User followers
                .requestMatchers("/api/users/*/following").permitAll() // User following
                .requestMatchers("/api/users/search").permitAll() // Search users

                // Recommendations endpoints - MISSING
                .requestMatchers("/api/recommendations").permitAll() // Create recommendations
                .requestMatchers("/api/recommendations/user/**").permitAll() // User recommendations
                .requestMatchers("/api/recommendations/my-given").permitAll() // Given recommendations
                .requestMatchers("/api/recommendations/my-received").permitAll() // Received recommendations
                .requestMatchers("/api/recommendations/search").permitAll() // Search recommendations
                .requestMatchers("/api/recommendations/**").permitAll() // Recommendation operations

                // Group endpoints
                .requestMatchers("/api/groups").permitAll() // List public groups
                .requestMatchers("/api/groups/*/join").permitAll() // Join groups
                .requestMatchers("/api/groups/*/leave").permitAll() // Leave groups
                .requestMatchers("/api/groups/*/members").permitAll() // Group members
                .requestMatchers("/api/groups/*/members/**").permitAll() // Member operations
                .requestMatchers("/api/groups/my-groups").permitAll() // User's groups
                .requestMatchers("/api/groups/**").permitAll() // Group operations

                // Messaging endpoints
                .requestMatchers("/api/messages/**").permitAll() // Private messaging
                .requestMatchers("/api/conversations/**").permitAll() // Conversations

                // Notification endpoints - MISSING
                .requestMatchers("/api/notifications").permitAll() // List notifications
                .requestMatchers("/api/notifications/*/read").permitAll() // Mark as read
                .requestMatchers("/api/notifications/mark-all-read").permitAll() // Mark all read
                .requestMatchers("/api/notifications/unread-count").permitAll() // Unread count
                .requestMatchers("/api/notifications/**").permitAll() // Notification operations

                // Scouting endpoints - MISSING
                .requestMatchers("/api/scouting-reports").permitAll() // List scouting reports
                .requestMatchers("/api/scouting-reports/player/**").permitAll() // Player reports
                .requestMatchers("/api/scouting-reports/scout/**").permitAll() // Scout reports
                .requestMatchers("/api/scouting-reports/search").permitAll() // Search reports
                .requestMatchers("/api/scouting-reports/recent").permitAll() // Recent reports
                .requestMatchers("/api/scouting-reports/top-rated").permitAll() // Top rated reports
                .requestMatchers("/api/scouting-reports/**").permitAll() // Scouting operations

                // Rating categories
                .requestMatchers("/api/rating-categories").permitAll() // List categories
                .requestMatchers("/api/rating-categories/**").permitAll() // Category operations

                .anyRequest().authenticated()
                );

        return http.build();
    }
}
