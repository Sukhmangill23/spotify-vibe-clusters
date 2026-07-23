package com.svc.repository;

import com.svc.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    List<Track> findByOwnerId(String ownerId);

    Optional<Track> findBySpotifyTrackIdAndOwnerId(String spotifyTrackId, String ownerId);

    long countByOwnerId(String ownerId);

    @Modifying
    @Query("DELETE FROM Track t WHERE t.ownerId = :ownerId")
    void deleteByOwnerId(@Param("ownerId") String ownerId);
}