package com.svc.controller;

import com.svc.entity.User;
import com.svc.repository.UserRepository;
import com.svc.service.IngestionService;
import com.svc.service.TokenRefreshService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IngestController {

    private final UserRepository userRepository;
    private final TokenRefreshService tokenRefreshService;
    private final IngestionService ingestionService;

    public IngestController(UserRepository userRepository,
                             TokenRefreshService tokenRefreshService,
                             IngestionService ingestionService) {
        this.userRepository = userRepository;
        this.tokenRefreshService = tokenRefreshService;
        this.ingestionService = ingestionService;
    }

    @PostMapping("/api/ingest")
    public IngestionService.IngestionResult ingest(OAuth2AuthenticationToken authToken) {
        String spotifyId = authToken.getPrincipal().getName();
        User user = userRepository.findById(spotifyId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + spotifyId));

        user = tokenRefreshService.ensureFreshToken(user);
        return ingestionService.ingest(user);
    }
}
