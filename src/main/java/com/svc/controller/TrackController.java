package com.svc.controller;

import com.svc.entity.Track;
import com.svc.repository.TrackRepository;
import com.svc.service.SimilarityService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    private final TrackRepository trackRepository;
    private final SimilarityService similarityService;

    public TrackController(TrackRepository trackRepository, SimilarityService similarityService) {
        this.trackRepository = trackRepository;
        this.similarityService = similarityService;
    }

    @GetMapping
    public List<Track> listTracks(OAuth2AuthenticationToken authToken) {
        String ownerId = authToken.getPrincipal().getName();
        return trackRepository.findByOwnerId(ownerId);
    }

    @GetMapping("/{id}/similar")
    public List<Track> similar(@PathVariable Long id,
                                @RequestParam(defaultValue = "10") int limit) {
        return similarityService.findSimilar(id, limit);
    }
}
