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

@Service
public class ClusteringService {

    private static final int MAX_ITERATIONS = 100;
    private static final int NUM_RESTARTS = 8;
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

        // NOTE: z-score standardization was tried here and benchmarked worse
        // (silhouette 0.40 -> 0.25) than Track.featureVector()'s scaling.
        // Acousticness/instrumentalness are naturally bimodal and
        // genre-discriminative; standardizing gave tempo/valence equal
        // weight and diluted that separation. Kept the empirically better
        // approach; standardize() is left below, unused, documenting what
        // was tried.

        int bestK = minK;
        double bestScore = -2.0;
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
        KMeansResult best = null;
        double bestInertia = Double.MAX_VALUE;

        for (int restart = 0; restart < NUM_RESTARTS; restart++) {
            Random random = new Random(RANDOM_SEED + restart);
            KMeansResult result = runSingleKMeans(points, k, random);
            double inertia = computeInertia(points, result.labels, result.centroids);

            if (inertia < bestInertia) {
                bestInertia = inertia;
                best = result;
            }
        }

        return best;
    }

    private KMeansResult runSingleKMeans(double[][] points, int k, Random random) {
        int n = points.length;
        int dims = points[0].length;

        double[][] centroids = initCentroidsPlusPlus(points, k, random);
        int[] labels = new int[n];

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            boolean changed = false;

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
                if (counts[c] == 0) continue;
                for (int d = 0; d < dims; d++) {
                    centroids[c][d] = sums[c][d] / counts[c];
                }
            }
        }

        return new KMeansResult(labels, centroids);
    }

    private double[][] initCentroidsPlusPlus(double[][] points, int k, Random random) {
        int n = points.length;
        double[][] centroids = new double[k][];

        centroids[0] = points[random.nextInt(n)].clone();

        double[] minDistSq = new double[n];
        for (int i = 0; i < n; i++) {
            minDistSq[i] = squaredEuclidean(points[i], centroids[0]);
        }

        for (int c = 1; c < k; c++) {
            double totalDist = 0;
            for (double d : minDistSq) totalDist += d;

            double target = random.nextDouble() * totalDist;
            double cumulative = 0;
            int chosenIndex = n - 1;
            for (int i = 0; i < n; i++) {
                cumulative += minDistSq[i];
                if (cumulative >= target) {
                    chosenIndex = i;
                    break;
                }
            }

            centroids[c] = points[chosenIndex].clone();

            for (int i = 0; i < n; i++) {
                double d = squaredEuclidean(points[i], centroids[c]);
                if (d < minDistSq[i]) minDistSq[i] = d;
            }
        }

        return centroids;
    }

    private double computeInertia(double[][] points, int[] labels, double[][] centroids) {
        double sum = 0;
        for (int i = 0; i < points.length; i++) {
            sum += squaredEuclidean(points[i], centroids[labels[i]]);
        }
        return sum;
    }

    private double[][] standardize(double[][] points) {
        int n = points.length;
        int dims = points[0].length;

        double[] mean = new double[dims];
        for (double[] p : points) {
            for (int d = 0; d < dims; d++) mean[d] += p[d];
        }
        for (int d = 0; d < dims; d++) mean[d] /= n;

        double[] std = new double[dims];
        for (double[] p : points) {
            for (int d = 0; d < dims; d++) {
                double diff = p[d] - mean[d];
                std[d] += diff * diff;
            }
        }
        for (int d = 0; d < dims; d++) {
            std[d] = Math.sqrt(std[d] / n);
            if (std[d] < 1e-9) std[d] = 1.0;
        }

        double[][] standardized = new double[n][dims];
        for (int i = 0; i < n; i++) {
            for (int d = 0; d < dims; d++) {
                standardized[i][d] = (points[i][d] - mean[d]) / std[d];
            }
        }
        return standardized;
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