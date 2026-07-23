package com.svc.repository;

import com.svc.entity.Track;
import com.svc.entity.TrackClusterAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrackClusterAssignmentRepository extends JpaRepository<TrackClusterAssignment, Long> {

    List<TrackClusterAssignment> findByClusterId(Long clusterId);

    @Modifying
    @Query("DELETE FROM TrackClusterAssignment a WHERE a.track.ownerId = :ownerId")
    void deleteByTrack_OwnerId(@Param("ownerId") String ownerId);

    @Query("SELECT a.track FROM TrackClusterAssignment a WHERE a.cluster.id = :clusterId")
    List<Track> findTracksByClusterId(@Param("clusterId") Long clusterId);
}