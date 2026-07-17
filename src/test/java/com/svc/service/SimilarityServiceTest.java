package com.svc.service;

import com.svc.entity.Track;
import com.svc.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimilarityServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @InjectMocks
    private SimilarityService similarityService;

    @Test
    void identicalVectorsHaveSimilarityOfOne() {
        double[] a = {0.5, 0.8, 0.3, 0.6, 0.1, 0.0};
        double[] b = {0.5, 0.8, 0.3, 0.6, 0.1, 0.0};

        assertEquals(1.0, similarityService.cosineSimilarity(a, b), 0.0001);
    }

    @Test
    void orthogonalVectorsHaveSimilarityOfZero() {
        double[] a = {1.0, 0.0};
        double[] b = {0.0, 1.0};

        assertEquals(0.0, similarityService.cosineSimilarity(a, b), 0.0001);
    }

    @Test
    void findSimilarExcludesTheTargetTrackItself() {
        Track target = new Track("spotify:1", "user1", "Song A", "Artist A");
        target.setEnergy(0.8);
        target.setValence(0.5);

        Track other = new Track("spotify:2", "user1", "Song B", "Artist B");
        other.setEnergy(0.79);
        other.setValence(0.51);

        setId(target, 1L);
        setId(other, 2L);

        when(trackRepository.findById(1L)).thenReturn(Optional.of(target));
        when(trackRepository.findByOwnerId("user1")).thenReturn(List.of(target, other));

        List<Track> results = similarityService.findSimilar(1L, 10);

        assertEquals(1, results.size());
        assertEquals(other.getId(), results.get(0).getId());
    }

    private void setId(Track track, Long id) {
        try {
            var field = Track.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(track, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
