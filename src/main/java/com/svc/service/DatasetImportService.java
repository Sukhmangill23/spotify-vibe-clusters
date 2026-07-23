package com.svc.service;

import com.svc.entity.Track;
import com.svc.repository.ClusterRepository;
import com.svc.repository.TrackClusterAssignmentRepository;
import com.svc.repository.TrackRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatasetImportService {

    public static final String DEMO_OWNER_ID = "dataset-demo";

    private final TrackRepository trackRepository;
    private final TrackClusterAssignmentRepository assignmentRepository;
    private final ClusterRepository clusterRepository;

    public DatasetImportService(TrackRepository trackRepository,
                                 TrackClusterAssignmentRepository assignmentRepository,
                                 ClusterRepository clusterRepository) {
        this.trackRepository = trackRepository;
        this.assignmentRepository = assignmentRepository;
        this.clusterRepository = clusterRepository;
    }

    @Transactional
    public ImportResult importSample(int limit) {
        assignmentRepository.deleteByTrack_OwnerId(DEMO_OWNER_ID);
        clusterRepository.deleteByOwnerId(DEMO_OWNER_ID);
        trackRepository.deleteByOwnerId(DEMO_OWNER_ID);

        List<Track> tracks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/spotify_tracks.csv").getInputStream(), StandardCharsets.UTF_8))) {

            reader.readLine();
            String line;
            int index = 0;

            while ((line = reader.readLine()) != null && tracks.size() < limit) {
                String[] cols = splitCsvLine(line);
                if (cols.length < 8) continue;

                try {
                    Track track = new Track("dataset-" + index, DEMO_OWNER_ID, cols[0], cols[1]);
                    track.setTempo(Double.parseDouble(cols[2]));
                    track.setEnergy(Double.parseDouble(cols[3]));
                    track.setValence(Double.parseDouble(cols[4]));
                    track.setDanceability(Double.parseDouble(cols[5]));
                    track.setAcousticness(Double.parseDouble(cols[6]));
                    track.setInstrumentalness(Double.parseDouble(cols[7]));
                    tracks.add(track);
                    index++;
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load dataset for demo import", e);
        }

        trackRepository.saveAll(tracks);
        return new ImportResult(tracks.size());
    }

    private String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
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

    public record ImportResult(int tracksImported) {
    }
}