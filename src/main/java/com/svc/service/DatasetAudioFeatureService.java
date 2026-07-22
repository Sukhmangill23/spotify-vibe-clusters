package com.svc.service;

import com.svc.dto.AudioFeaturesDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Spotify deprecated the /audio-features endpoint for all apps created after
 * November 27, 2024 (permanent 403, no official replacement). This service
 * substitutes a public dataset of ~62k tracks with the same audio-feature
 * columns Spotify used to expose, matched to a user's library by normalized
 * track name + primary artist.
 *
 * This is a fuzzy join, not an exact one: streaming service IDs aren't
 * available for arbitrary public datasets, so name+artist matching is the
 * practical join key. matchRate() reports how much of a user's real library
 * this actually covers, which is the honest number to put on a resume rather
 * than assuming 100% coverage.
 */
@Service
public class DatasetAudioFeatureService {

    private final Map<String, AudioFeaturesDTO> byNormalizedKey = new HashMap<>();

    @PostConstruct
    public void loadDataset() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/spotify_tracks.csv").getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine(); // track_name,artist_name,tempo,energy,valence,danceability,acousticness,instrumentalness
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = splitCsvLine(line);
                if (cols.length < 8) continue;

                try {
                    AudioFeaturesDTO dto = new AudioFeaturesDTO();
                    dto.setTempo(Double.parseDouble(cols[2]));
                    dto.setEnergy(Double.parseDouble(cols[3]));
                    dto.setValence(Double.parseDouble(cols[4]));
                    dto.setDanceability(Double.parseDouble(cols[5]));
                    dto.setAcousticness(Double.parseDouble(cols[6]));
                    dto.setInstrumentalness(Double.parseDouble(cols[7]));

                    String key = normalizedKey(cols[0], cols[1]);
                    byNormalizedKey.putIfAbsent(key, dto);
                } catch (NumberFormatException ignored) {
                    // skip malformed rows rather than failing the whole load
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load audio feature dataset", e);
        }
    }

    public AudioFeaturesDTO lookup(String trackName, String artistName) {
        return byNormalizedKey.get(normalizedKey(trackName, artistName));
    }

    public int datasetSize() {
        return byNormalizedKey.size();
    }

    private String normalizedKey(String trackName, String artistName) {
        return normalize(trackName) + "|" + normalize(artistName);
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase()
                .replaceAll("\\(.*?\\)", "")   // drop "(feat. X)", "(Remastered)", etc.
                .replaceAll("[^a-z0-9 ]", "")
                .trim()
                .replaceAll("\\s+", " ");
    }

    // Minimal CSV line splitter handling quoted fields with embedded commas,
    // since track/album titles frequently contain commas and quotes.
    private String[] splitCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
