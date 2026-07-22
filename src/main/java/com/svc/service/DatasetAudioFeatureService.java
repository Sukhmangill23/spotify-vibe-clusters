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

@Service
public class DatasetAudioFeatureService {

    private final Map<String, AudioFeaturesDTO> byNameAndArtist = new HashMap<>();
    private final Map<String, AudioFeaturesDTO> byNameOnly = new HashMap<>();
    private final Map<String, Integer> nameOnlyCollisionCount = new HashMap<>();

    @PostConstruct
    public void loadDataset() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/spotify_tracks.csv").getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine();
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

                    String normName = normalizeName(cols[0]);
                    String normArtist = normalizePrimaryArtist(cols[1]);

                    byNameAndArtist.putIfAbsent(normName + "|" + normArtist, dto);

                    if (normName.length() >= 4) {
                        if (byNameOnly.containsKey(normName)) {
                            nameOnlyCollisionCount.merge(normName, 1, Integer::sum);
                        } else {
                            byNameOnly.put(normName, dto);
                            nameOnlyCollisionCount.put(normName, 1);
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load audio feature dataset", e);
        }
    }

    public AudioFeaturesDTO lookup(String trackName, String artistName) {
        String normName = normalizeName(trackName);
        String normArtist = normalizePrimaryArtist(artistName);

        AudioFeaturesDTO exact = byNameAndArtist.get(normName + "|" + normArtist);
        if (exact != null) return exact;

        if (normName.length() >= 4 && nameOnlyCollisionCount.getOrDefault(normName, 0) == 1) {
            return byNameOnly.get(normName);
        }

        return null;
    }

    public int datasetSize() {
        return byNameAndArtist.size();
    }

    private String normalizeName(String trackName) {
        if (trackName == null) return "";
        String s = trackName.toLowerCase();
        s = s.replaceAll("\\(.*?\\)", " ");
        s = s.replaceAll("\\[.*?\\]", " ");
        s = s.replaceAll("\\s*-\\s*(remix|live|acoustic|remastered|mono|stereo|radio edit|single version|album version|extended|instrumental|edit)\\b.*", " ");
        s = s.replaceAll("[^a-z0-9 ]", " ");
        s = s.trim().replaceAll("\\s+", " ");
        return s;
    }

    private String normalizePrimaryArtist(String artistName) {
        if (artistName == null) return "";
        String s = artistName.toLowerCase();
        s = s.split(",")[0];
        s = s.split("&")[0];
        s = s.split("\\bfeat\\.?\\b")[0];
        s = s.split("\\bft\\.?\\b")[0];
        s = s.split("\\bwith\\b")[0];
        s = s.replaceAll("[^a-z0-9 ]", " ");
        s = s.trim().replaceAll("\\s+", " ");
        return s;
    }

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