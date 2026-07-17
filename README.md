# Spotify Vibe Clusters

A Spring Boot backend that connects to your Spotify account, pulls your
listening history and audio features, and groups your library into
"vibe" clusters using a from-scratch k-means implementation. Also
exposes a track-to-track recommendation endpoint based on cosine
similarity over audio feature vectors.

## Features

- Spotify OAuth2 login
- Ingests saved/top tracks and their audio features (tempo, energy,
  valence, danceability, acousticness, instrumentalness), batching
  requests against Spotify's audio-features endpoint (100 tracks/call)
- K-means clustering implemented from scratch, tuned via silhouette
  score across a range of k values
- Cosine-similarity recommendation endpoint
- Automatic OAuth2 access token refresh
- REST API documented with Swagger UI

## Stack

Java 17, Spring Boot, Spring Security (OAuth2 Client), Spring Data
JPA, PostgreSQL, JUnit 5 + Mockito, springdoc-openapi.

## Setup

1. Create a Spotify Developer app at
   https://developer.spotify.com/dashboard and note the client ID/secret.
2. Copy `src/main/resources/application-example.yml` to
   `application-local.yml` and fill in your Spotify credentials and
   local Postgres connection info.
3. `mvn spring-boot:run`
4. Visit `/oauth2/authorization/spotify` to log in.
5. `POST /api/ingest` to pull your library.
6. `GET /api/clusters` to see your vibe clusters.
7. `GET /api/tracks/{id}/similar` for recommendations.

## Status

Work in progress — see commit history for build order.
