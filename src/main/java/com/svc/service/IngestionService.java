package com.svc.service;

import com.svc.dto.AudioFeaturesDTO;
import com.svc.dto.SpotifyTrackDTO;
import com.svc.entity.Track;
import com.svc.entity.User;
import com.svc.repository.TrackRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pulls a user's library from Spotify and persists tracks + audio features.
 * Tracks how many audio-feature API calls batching saved, since that number
 * feeds directly into the resume metric for this project.
 */
@Service
public class IngestionService {

    private final SpotifyClientService spotifyClientService;
    private final TrackRepository trackRepository;

    public IngestionService(SpotifyClientService spotifyClientService, TrackRepository trackRepository) {
        this.spotifyClientService = spotifyClientService;
        this.trackRepository = trackRepository;
    }

    public IngestionResult ingest(User user) {
        List<SpotifyTrackDTO> savedTracks = spotifyClientService.fetchSavedTracks(user);
        List<String> trackIds = savedTracks.stream().map(SpotifyTrackDTO::getId).collect(Collectors.toList());

        List<AudioFeaturesDTO> features = spotifyClientService.fetchAudioFeaturesBatched(user, trackIds);
        Map<String, AudioFeaturesDTO> featuresById = new HashMap<>();
        for (AudioFeaturesDTO f : features) {
            featuresById.put(f.getId(), f);
        }

        int savedCount = 0;
        for (SpotifyTrackDTO dto : savedTracks) {
            AudioFeaturesDTO f = featuresById.get(dto.getId());
            if (f == null) continue; // some tracks (podcasts, local files) have no audio features

            Track track = trackRepository.findBySpotifyTrackIdAndOwnerId(dto.getId(), user.getSpotifyId())
                    .orElse(new Track(dto.getId(), user.getSpotifyId(), dto.getName(), dto.getArtist()));

            track.setTempo(f.getTempo());
            track.setEnergy(f.getEnergy());
            track.setValence(f.getValence());
            track.setDanceability(f.getDanceability());
            track.setAcousticness(f.getAcousticness());
            track.setInstrumentalness(f.getInstrumentalness());

            trackRepository.save(track);
            savedCount++;
        }

        int actualCalls = spotifyClientService.batchCallCount(trackIds.size());
        int unbatchedCalls = trackIds.size(); // one call per track if we hadn't batched
        double reductionPct = unbatchedCalls == 0 ? 0
                : (1 - (actualCalls / (double) unbatchedCalls)) * 100;

        return new IngestionResult(savedCount, actualCalls, unbatchedCalls, reductionPct);
    }

    public record IngestionResult(int tracksIngested, int actualApiCalls, int unbatchedApiCalls,
                                   double apiCallReductionPct) {
    }
}
