package com.svc.repository;

import com.svc.entity.TrackClusterAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackClusterAssignmentRepository extends JpaRepository<TrackClusterAssignment, Long> {

    List<TrackClusterAssignment> findByClusterId(Long clusterId);

    void deleteByTrack_OwnerId(String ownerId);
}
