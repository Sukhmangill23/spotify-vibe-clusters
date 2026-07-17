package com.svc.service;

import com.svc.entity.User;
import com.svc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

/**
 * Transparently refreshes a user's Spotify access token when it has expired,
 * so callers never have to think about token lifecycle themselves.
 */
@Service
public class TokenRefreshService {

    private final RestClient restClient;
    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.spotify.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret:}")
    private String clientSecret;

    public TokenRefreshService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restClient = RestClient.builder()
                .baseUrl("https://accounts.spotify.com/api/token")
                .build();
    }

    public User ensureFreshToken(User user) {
        if (!user.isTokenExpired()) {
            return user;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", user.getRefreshToken());
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map<String, Object> response = restClient.post()
                .body(form)
                .retrieve()
                .body(Map.class);

        if (response != null && response.get("access_token") != null) {
            user.setAccessToken((String) response.get("access_token"));
            int expiresIn = ((Number) response.getOrDefault("expires_in", 3600)).intValue();
            user.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
            if (response.get("refresh_token") != null) {
                user.setRefreshToken((String) response.get("refresh_token"));
            }
            userRepository.save(user);
        }

        return user;
    }
}
