package com.svc.controller;

import com.svc.entity.Cluster;
import com.svc.entity.Track;
import com.svc.entity.TrackClusterAssignment;
import com.svc.repository.ClusterRepository;
import com.svc.repository.TrackClusterAssignmentRepository;
import com.svc.service.ClusteringService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clusters")
public class ClusterController {

    private final ClusteringService clusteringService;
    private final ClusterRepository clusterRepository;
    private final TrackClusterAssignmentRepository assignmentRepository;

    public ClusterController(ClusteringService clusteringService,
                              ClusterRepository clusterRepository,
                              TrackClusterAssignmentRepository assignmentRepository) {
        this.clusteringService = clusteringService;
        this.clusterRepository = clusterRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @PostMapping
    public ClusteringService.ClusteringResult recluster(OAuth2AuthenticationToken authToken) {
        String ownerId = authToken.getPrincipal().getName();
        return clusteringService.clusterLibrary(ownerId);
    }

    @GetMapping
    public List<Cluster> listClusters(OAuth2AuthenticationToken authToken) {
        String ownerId = authToken.getPrincipal().getName();
        return clusterRepository.findByOwnerId(ownerId);
    }

@GetMapping("/{id}/tracks")
    public List<Track> tracksInCluster(@PathVariable Long id) {
        return assignmentRepository.findTracksByClusterId(id);
    }
}
