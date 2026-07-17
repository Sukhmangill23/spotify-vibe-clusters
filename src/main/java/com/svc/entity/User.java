package com.svc.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @Column(name = "spotify_id")
    private String spotifyId;

    private String displayName;
    private String email;

    @Column(length = 2000)
    private String accessToken;

    @Column(length = 2000)
    private String refreshToken;

    private Instant tokenExpiresAt;

    protected User() {
    }

    public User(String spotifyId, String displayName, String email) {
        this.spotifyId = spotifyId;
        this.displayName = displayName;
        this.email = email;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(Instant tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public boolean isTokenExpired() {
        return tokenExpiresAt == null || Instant.now().isAfter(tokenExpiresAt);
    }
}
