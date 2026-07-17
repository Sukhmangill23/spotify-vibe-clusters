package com.svc.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cluster")
public class Cluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "cluster_index", nullable = false)
    private int clusterIndex;

    // Centroid coordinates stored as comma-separated doubles (kept simple; a
    // separate table would be overkill for a 6-dimension vector)
    @Column(name = "centroid", nullable = false, length = 500)
    private String centroid;

    protected Cluster() {
    }

    public Cluster(String ownerId, int clusterIndex, double[] centroid) {
        this.ownerId = ownerId;
        this.clusterIndex = clusterIndex;
        setCentroidVector(centroid);
    }

    public double[] getCentroidVector() {
        String[] parts = centroid.split(",");
        double[] vec = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vec[i] = Double.parseDouble(parts[i]);
        }
        return vec;
    }

    public void setCentroidVector(double[] vec) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vec.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vec[i]);
        }
        this.centroid = sb.toString();
    }

    public Long getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public int getClusterIndex() {
        return clusterIndex;
    }
}
