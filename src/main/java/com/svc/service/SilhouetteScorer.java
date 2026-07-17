package com.svc.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Computes the standard silhouette score for a clustering assignment:
 * for each point, (b - a) / max(a, b), where a = mean intra-cluster
 * distance and b = mean distance to the nearest other cluster.
 * Score ranges from -1 (bad) to 1 (well-separated, cohesive clusters).
 */
@Component
public class SilhouetteScorer {

    public double score(double[][] points, int[] labels) {
        int n = points.length;
        int k = Arrays.stream(labels).max().orElse(0) + 1;

        double total = 0;
        int counted = 0;

        for (int i = 0; i < n; i++) {
            double a = meanDistanceToOwnCluster(points, labels, i);
            double b = meanDistanceToNearestOtherCluster(points, labels, i, k);

            if (a == -1 || b == -1) continue; // singleton cluster, silhouette undefined for this point

            double s = (b - a) / Math.max(a, b);
            total += s;
            counted++;
        }

        return counted == 0 ? 0 : total / counted;
    }

    private double meanDistanceToOwnCluster(double[][] points, int[] labels, int i) {
        int cluster = labels[i];
        double sum = 0;
        int count = 0;
        for (int j = 0; j < points.length; j++) {
            if (j != i && labels[j] == cluster) {
                sum += distance(points[i], points[j]);
                count++;
            }
        }
        return count == 0 ? -1 : sum / count;
    }

    private double meanDistanceToNearestOtherCluster(double[][] points, int[] labels, int i, int k) {
        double best = Double.MAX_VALUE;
        boolean found = false;

        for (int cluster = 0; cluster < k; cluster++) {
            if (cluster == labels[i]) continue;
            double sum = 0;
            int count = 0;
            for (int j = 0; j < points.length; j++) {
                if (labels[j] == cluster) {
                    sum += distance(points[i], points[j]);
                    count++;
                }
            }
            if (count > 0) {
                found = true;
                best = Math.min(best, sum / count);
            }
        }

        return found ? best : -1;
    }

    private double distance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}

