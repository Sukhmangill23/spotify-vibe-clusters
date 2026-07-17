package com.svc.service;

import com.svc.entity.Cluster;
import com.svc.entity.Track;
import com.svc.entity.TrackClusterAssignment;
import com.svc.repository.ClusterRepository;
import com.svc.repository.TrackClusterAssignmentRepository;
import com.svc.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * From-scratch k-means clustering over track audio-feature vectors, with the
 * cluster count (k) chosen by trying a range of values and picking the one
 * with the best silhouette score (see SilhouetteScorer).
 */
@Service
public class ClusteringService {

    private static final int MAX_ITERATIONS = 100;
    private static final long RANDOM_SEED = 42L;

    @Value("${vibeclusters.clustering.min-k:4}")
    private int minK;

    @Value("${vibeclusters.clustering.max-k:10}")
    private int maxK;

    private final TrackRepository trackRepository;
    private final ClusterRepository clusterRepository;
    private final TrackClusterAssignmentRepository assignmentRepository;
    private final SilhouetteScorer silhouetteScorer;

    public ClusteringService(TrackRepository trackRepository,
                              ClusterRepository clusterRepository,
                              TrackClusterAssignmentRepository assignmentRepository,
                              SilhouetteScorer silhouetteScorer) {
        this.trackRepository = trackRepository;
        this.clusterRepository = clusterRepository;
        this.assignmentRepository = assignmentRepository;
        this.silhouetteScorer = silhouetteScorer;
    }

    @Transactional
    public ClusteringResult clusterLibrary(String ownerId) {
        List<Track> tracks = trackRepository.findByOwnerId(ownerId);
        if (tracks.size() < minK) {
            throw new IllegalStateException("Not enough tracks to cluster: need at least " + minK);
        }

        double[][] points = new double[tracks.size()][];
        for (int i = 0; i < tracks.size(); i++) {
            points[i] = tracks.get(i).featureVector();
        }

        int bestK = minK;
        double bestScore = -2.0; // silhouette score ranges [-1, 1]
        int[] bestLabels = null;
        double[][] bestCentroids = null;

        for (int k = minK; k <= Math.min(maxK, tracks.size() - 1); k++) {
            KMeansResult result = runKMeans(points, k);
            double score = silhouetteScorer.score(points, result.labels);
            if (score > bestScore) {
                bestScore = score;
                bestK = k;
                bestLabels = result.labels;
                bestCentroids = result.centroids;
            }
        }

        // Persist: wipe old clusters/assignments for this user, write the new ones
        assignmentRepository.deleteByTrack_OwnerId(ownerId);
        clusterRepository.deleteByOwnerId(ownerId);

        Cluster[] clusters = new Cluster[bestK];
        for (int i = 0; i < bestK; i++) {
            clusters[i] = clusterRepository.save(new Cluster(ownerId, i, bestCentroids[i]));
        }

        for (int i = 0; i < tracks.size(); i++) {
            assignmentRepository.save(new TrackClusterAssignment(tracks.get(i), clusters[bestLabels[i]]));
        }

        return new ClusteringResult(bestK, bestScore);
    }

    private KMeansResult runKMeans(double[][] points, int k) {
        Random random = new Random(RANDOM_SEED);
        int n = points.length;
        int dims = points[0].length;

        // k-means++ style seeding would be nicer, but random init with enough
        // iterations converges fine at this dataset size
        double[][] centroids = new double[k][];
        Set<Integer> chosen = new HashSet<>();
        while (chosen.size() < k) {
            chosen.add(random.nextInt(n));
        }
        int idx = 0;
        for (int i : chosen) {
            centroids[idx++] = points[i].clone();
        }

        int[] labels = new int[n];

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            boolean changed = false;

            // Assignment step
            for (int i = 0; i < n; i++) {
                int closest = 0;
                double closestDist = Double.MAX_VALUE;
                for (int c = 0; c < k; c++) {
                    double dist = squaredEuclidean(points[i], centroids[c]);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = c;
                    }
                }
                if (labels[i] != closest) {
                    changed = true;
                    labels[i] = closest;
                }
            }

            if (!changed && iter > 0) break;

            // Update step
            double[][] sums = new double[k][dims];
            int[] counts = new int[k];
            for (int i = 0; i < n; i++) {
                int c = labels[i];
                counts[c]++;
                for (int d = 0; d < dims; d++) {
                    sums[c][d] += points[i][d];
                }
            }
            for (int c = 0; c < k; c++) {
                if (counts[c] == 0) continue; // keep previous centroid if a cluster went empty
                for (int d = 0; d < dims; d++) {
                    centroids[c][d] = sums[c][d] / counts[c];
                }
            }
        }

        return new KMeansResult(labels, centroids);
    }

    private double squaredEuclidean(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }

    private record KMeansResult(int[] labels, double[][] centroids) {
    }

    public record ClusteringResult(int k, double silhouetteScore) {
    }
}
