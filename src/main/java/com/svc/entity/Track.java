package com.svc.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "track", uniqueConstraints = @UniqueConstraint(columnNames = {"spotify_track_id", "owner_id"}))
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spotify_track_id", nullable = false)
    private String spotifyTrackId;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    private String name;
    private String artist;

    // Audio features, all normalized 0.0-1.0 by Spotify except tempo (BPM)
    private double tempo;
    private double energy;
    private double valence;
    private double danceability;
    private double acousticness;
    private double instrumentalness;

    protected Track() {
    }

    public Track(String spotifyTrackId, String ownerId, String name, String artist) {
        this.spotifyTrackId = spotifyTrackId;
        this.ownerId = ownerId;
        this.name = name;
        this.artist = artist;
    }

    public double[] featureVector() {
        return new double[]{
                // tempo is scaled down since it's on a much larger numeric range (~60-200)
                // than the other 0-1 features; dividing by 200 keeps clustering distances fair
                tempo / 200.0,
                energy,
                valence,
                danceability,
                acousticness,
                instrumentalness
        };
    }

    public Long getId() {
        return id;
    }

    public String getSpotifyTrackId() {
        return spotifyTrackId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public double getTempo() {
        return tempo;
    }

    public void setTempo(double tempo) {
        this.tempo = tempo;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getValence() {
        return valence;
    }

    public void setValence(double valence) {
        this.valence = valence;
    }

    public double getDanceability() {
        return danceability;
    }

    public void setDanceability(double danceability) {
        this.danceability = danceability;
    }

    public double getAcousticness() {
        return acousticness;
    }

    public void setAcousticness(double acousticness) {
        this.acousticness = acousticness;
    }

    public double getInstrumentalness() {
        return instrumentalness;
    }

    public void setInstrumentalness(double instrumentalness) {
        this.instrumentalness = instrumentalness;
    }
}
