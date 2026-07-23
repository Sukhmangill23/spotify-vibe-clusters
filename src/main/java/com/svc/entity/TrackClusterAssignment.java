package com.svc.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "track_cluster_assignment")
public class TrackClusterAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private Cluster cluster;

    protected TrackClusterAssignment() {
    }

    public TrackClusterAssignment(Track track, Cluster cluster) {
        this.track = track;
        this.cluster = cluster;
    }

    public Long getId() {
        return id;
    }

    public Track getTrack() {
        return track;
    }

    public Cluster getCluster() {
        return cluster;
    }
}