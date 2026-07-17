package com.svc.service;

import com.svc.entity.Track;
import com.svc.repository.TrackRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Recommends tracks similar to a given track using cosine similarity over
 * the normalized audio-feature vectors.
 */
@Service
public class SimilarityService {

    private final TrackRepository trackRepository;

    public SimilarityService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    public List<Track> findSimilar(Long trackId, int limit) {
        Track target = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found: " + trackId));

        List<Track> candidates = trackRepository.findByOwnerId(target.getOwnerId());
        double[] targetVec = target.featureVector();

        return candidates.stream()
                .filter(t -> !t.getId().equals(trackId))
                .sorted(Comparator.comparingDouble(
                        (Track t) -> -cosineSimilarity(targetVec, t.featureVector())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
