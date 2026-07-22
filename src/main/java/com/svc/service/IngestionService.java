package com.svc.service;

import com.svc.dto.AudioFeaturesDTO;
import com.svc.dto.SpotifyTrackDTO;
import com.svc.entity.Track;
import com.svc.entity.User;
import com.svc.repository.TrackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Pulls a user's library from Spotify (this part still works — only the
 * audio-features endpoint was deprecated) and enriches each track with
 * audio features via DatasetAudioFeatureService instead of Spotify's
 * /audio-features endpoint, which returns a permanent 403 for any app
 * created after November 27, 2024.
 *
 * Tracks a real match-rate metric (how many of the user's saved tracks were
 * found in the dataset) rather than assuming full coverage, since a
 * name+artist fuzzy join will legitimately miss some tracks.
 */
@Service
public class IngestionService {

    private final SpotifyClientService spotifyClientService;
    private final TrackRepository trackRepository;
    private final DatasetAudioFeatureService datasetAudioFeatureService;

    public IngestionService(SpotifyClientService spotifyClientService,
                             TrackRepository trackRepository,
                             DatasetAudioFeatureService datasetAudioFeatureService) {
        this.spotifyClientService = spotifyClientService;
        this.trackRepository = trackRepository;
        this.datasetAudioFeatureService = datasetAudioFeatureService;
    }

    public IngestionResult ingest(User user) {
        List<SpotifyTrackDTO> savedTracks = spotifyClientService.fetchSavedTracks(user);

        int matchedCount = 0;
        for (SpotifyTrackDTO dto : savedTracks) {
            AudioFeaturesDTO f = datasetAudioFeatureService.lookup(dto.getName(), dto.getArtist());
            if (f == null) continue; // track not found in the public dataset; skip rather than fabricate features

            Track track = trackRepository.findBySpotifyTrackIdAndOwnerId(dto.getId(), user.getSpotifyId())
                    .orElse(new Track(dto.getId(), user.getSpotifyId(), dto.getName(), dto.getArtist()));

            track.setTempo(f.getTempo());
            track.setEnergy(f.getEnergy());
            track.setValence(f.getValence());
            track.setDanceability(f.getDanceability());
            track.setAcousticness(f.getAcousticness());
            track.setInstrumentalness(f.getInstrumentalness());

            trackRepository.save(track);
            matchedCount++;
        }

        double matchRatePct = savedTracks.isEmpty() ? 0
                : (matchedCount / (double) savedTracks.size()) * 100;

        return new IngestionResult(savedTracks.size(), matchedCount, matchRatePct,
                datasetAudioFeatureService.datasetSize());
    }

    public record IngestionResult(int tracksInLibrary, int tracksMatched, double matchRatePct,
                                   int datasetSize) {
    }
}
