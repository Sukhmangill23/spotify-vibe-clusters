package com.svc.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusteringServiceTest {

    private final SilhouetteScorer scorer = new SilhouetteScorer();

    @Test
    void wellSeparatedClustersScoreHigh() {
        // Two tight, far-apart clusters -> silhouette should be close to 1
        double[][] points = {
                {0.0, 0.0}, {0.1, 0.0}, {0.0, 0.1},
                {5.0, 5.0}, {5.1, 5.0}, {5.0, 5.1}
        };
        int[] labels = {0, 0, 0, 1, 1, 1};

        double score = scorer.score(points, labels);

        assertTrue(score > 0.8, "Expected a high silhouette score for well-separated clusters, got " + score);
    }

    @Test
    void overlappingRandomAssignmentScoresLow() {
        // Same points, but labels scrambled so clusters overlap in space
        double[][] points = {
                {0.0, 0.0}, {0.1, 0.0}, {0.0, 0.1},
                {5.0, 5.0}, {5.1, 5.0}, {5.0, 5.1}
        };
        int[] labels = {0, 1, 0, 1, 0, 1};

        double score = scorer.score(points, labels);

        assertTrue(score < 0.8, "Expected a lower silhouette score for a poor assignment, got " + score);
    }

    @Test
    void singleClusterScoresZeroOrUndefined() {
        double[][] points = {{1.0, 1.0}, {1.1, 1.1}, {0.9, 0.9}};
        int[] labels = {0, 0, 0};

        double score = scorer.score(points, labels);

        assertEquals(0.0, score, 0.0001, "A single cluster has no 'other cluster' distance, score should default to 0");
    }
}
