package com.svc.controller;

import com.svc.entity.User;
import com.svc.repository.UserRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/me")
    public User me(OAuth2AuthenticationToken authToken) {
        OAuth2User principal = authToken.getPrincipal();
        String spotifyId = principal.getName();
        return userRepository.findById(spotifyId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + spotifyId));
    }
}
