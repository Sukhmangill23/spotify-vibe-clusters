package com.svc.repository;

import com.svc.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    List<Track> findByOwnerId(String ownerId);

    Optional<Track> findBySpotifyTrackIdAndOwnerId(String spotifyTrackId, String ownerId);

    long countByOwnerId(String ownerId);
}
