package com.svc.repository;

import com.svc.entity.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClusterRepository extends JpaRepository<Cluster, Long> {

    List<Cluster> findByOwnerId(String ownerId);

    void deleteByOwnerId(String ownerId);
}
