package com.svc.controller;

import com.svc.entity.Cluster;
import com.svc.entity.Track;
import com.svc.entity.TrackClusterAssignment;
import com.svc.repository.ClusterRepository;
import com.svc.repository.TrackClusterAssignmentRepository;
import com.svc.service.ClusteringService;
import com.svc.service.DatasetImportService;
import com.svc.service.SimilarityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DatasetImportService datasetImportService;
    private final ClusteringService clusteringService;
    private final ClusterRepository clusterRepository;
    private final TrackClusterAssignmentRepository assignmentRepository;
    private final SimilarityService similarityService;

    public DemoController(DatasetImportService datasetImportService,
                           ClusteringService clusteringService,
                           ClusterRepository clusterRepository,
                           TrackClusterAssignmentRepository assignmentRepository,
                           SimilarityService similarityService) {
        this.datasetImportService = datasetImportService;
        this.clusteringService = clusteringService;
        this.clusterRepository = clusterRepository;
        this.assignmentRepository = assignmentRepository;
        this.similarityService = similarityService;
    }

    @RequestMapping(value = "/import", method = {RequestMethod.GET, RequestMethod.POST})
    public com.svc.service.ImportResult importSample(@RequestParam(defaultValue = "5000") int limit) {
        return datasetImportService.importSample(limit);
    }

    @RequestMapping(value = "/cluster", method = {RequestMethod.GET, RequestMethod.POST})
    public ClusteringService.ClusteringResult cluster() {
        return clusteringService.clusterLibrary(DatasetImportService.DEMO_OWNER_ID);
    }

    @GetMapping("/clusters")
    public List<Cluster> listClusters() {
        return clusterRepository.findByOwnerId(DatasetImportService.DEMO_OWNER_ID);
    }

    @GetMapping("/clusters/{id}/tracks")
    public List<Track> tracksInCluster(@PathVariable Long id) {
        return assignmentRepository.findTracksByClusterId(id);
    }

    @GetMapping("/tracks/{id}/similar")
    public List<Track> similar(@PathVariable Long id, @RequestParam(defaultValue = "10") int limit) {
        return similarityService.findSimilar(id, limit);
    }
}