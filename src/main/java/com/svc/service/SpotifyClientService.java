package com.svc.service;

import com.svc.dto.AudioFeaturesDTO;
import com.svc.dto.SpotifyTrackDTO;
import com.svc.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Wraps calls to Spotify's Web API: fetching a user's saved tracks and their
 * audio features, plus refreshing an expired access token.
 *
 * Batches audio-feature lookups in groups of 100 (Spotify's max per request)
 * instead of one call per track, which is the source of the API-call
 * reduction figure on the resume bullet — see IngestionService for where
 * that count is measured.
 */
@Service
public class SpotifyClientService {

    private static final int AUDIO_FEATURES_BATCH_SIZE = 100;

    private final RestClient restClient;

    @Value("${spring.security.oauth2.client.registration.spotify.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret:}")
    private String clientSecret;

    public SpotifyClientService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.spotify.com/v1")
                .build();
    }

    public List<SpotifyTrackDTO> fetchSavedTracks(User user) {
        List<SpotifyTrackDTO> tracks = new ArrayList<>();
        String nextUrl = "/me/tracks?limit=50";

        while (nextUrl != null) {
            Map<String, Object> response = restClient.get()
                    .uri(nextUrl)
                    .header("Authorization", "Bearer " + user.getAccessToken())
                    .retrieve()
                    .body(Map.class);

            if (response == null) break;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            for (Map<String, Object> item : items) {
                @SuppressWarnings("unchecked")
                Map<String, Object> track = (Map<String, Object>) item.get("track");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> artists = (List<Map<String, Object>>) track.get("artists");
                String artistName = artists.isEmpty() ? "Unknown" : (String) artists.get(0).get("name");

                tracks.add(new SpotifyTrackDTO(
                        (String) track.get("id"),
                        (String) track.get("name"),
                        artistName
                ));
            }

            nextUrl = (String) response.get("next");
        }

        return tracks;
    }

    /**
     * Fetches audio features for the given track IDs, chunking requests into
     * groups of AUDIO_FEATURES_BATCH_SIZE so N tracks costs
     * ceil(N / 100) calls instead of N calls.
     */
    public List<AudioFeaturesDTO> fetchAudioFeaturesBatched(User user, List<String> trackIds) {
        List<AudioFeaturesDTO> results = new ArrayList<>();

        for (int i = 0; i < trackIds.size(); i += AUDIO_FEATURES_BATCH_SIZE) {
            List<String> batch = trackIds.subList(i, Math.min(i + AUDIO_FEATURES_BATCH_SIZE, trackIds.size()));
            String ids = String.join(",", batch);

            Map<String, Object> response = restClient.get()
                    .uri("/audio-features?ids=" + ids)
                    .header("Authorization", "Bearer " + user.getAccessToken())
                    .retrieve()
                    .body(Map.class);

            if (response == null) continue;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("audio_features");
            for (Map<String, Object> f : features) {
                if (f == null) continue;
                AudioFeaturesDTO dto = new AudioFeaturesDTO();
                dto.setId((String) f.get("id"));
                dto.setTempo(((Number) f.get("tempo")).doubleValue());
                dto.setEnergy(((Number) f.get("energy")).doubleValue());
                dto.setValence(((Number) f.get("valence")).doubleValue());
                dto.setDanceability(((Number) f.get("danceability")).doubleValue());
                dto.setAcousticness(((Number) f.get("acousticness")).doubleValue());
                dto.setInstrumentalness(((Number) f.get("instrumentalness")).doubleValue());
                results.add(dto);
            }
        }

        return results;
    }

    public int batchCallCount(int trackCount) {
        return (int) Math.ceil(trackCount / (double) AUDIO_FEATURES_BATCH_SIZE);
    }
}
