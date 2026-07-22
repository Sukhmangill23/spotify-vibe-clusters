package com.svc.repository;

import com.svc.entity.Track;
import com.svc.entity.TrackClusterAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrackClusterAssignmentRepository extends JpaRepository<TrackClusterAssignment, Long> {

    List<TrackClusterAssignment> findByClusterId(Long clusterId);

    void deleteByTrack_OwnerId(String ownerId);

    @Query("SELECT a.track FROM TrackClusterAssignment a WHERE a.cluster.id = :clusterId")
    List<Track> findTracksByClusterId(@Param("clusterId") Long clusterId);
}