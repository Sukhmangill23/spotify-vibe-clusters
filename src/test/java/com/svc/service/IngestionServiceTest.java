package com.svc.service;

import com.svc.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class IngestionServiceTest {

    @Test
    void batchCallCountRoundsUpForPartialFinalBatch() {
        SpotifyClientService client = new SpotifyClientService();

        assertEquals(1, client.batchCallCount(50));
        assertEquals(1, client.batchCallCount(100));
        assertEquals(2, client.batchCallCount(101));
        assertEquals(3, client.batchCallCount(250));
    }

    @Test
    void apiCallReductionIsHighForLargeLibraries() {
        SpotifyClientService client = new SpotifyClientService();
        int trackCount = 2500;

        int actualCalls = client.batchCallCount(trackCount);
        int unbatchedCalls = trackCount;
        double reductionPct = (1 - (actualCalls / (double) unbatchedCalls)) * 100;

        assertEquals(25, actualCalls);
        assertTrue(reductionPct > 95.0, "Expected >95% reduction for a 2500-track library, got " + reductionPct);
    }

    @Test
    void userTokenIsExpiredWhenPastExpiryInstant() {
        User user = new User("spotify123", "Test User", "test@example.com");
        user.setTokenExpiresAt(Instant.now().minusSeconds(10));

        assertTrue(user.isTokenExpired());
    }

    @Test
    void userTokenIsNotExpiredWhenBeforeExpiryInstant() {
        User user = new User("spotify123", "Test User", "test@example.com");
        user.setTokenExpiresAt(Instant.now().plusSeconds(3600));

        assertFalse(user.isTokenExpired());
    }

    @Test
    void userTokenWithNoExpiryIsTreatedAsExpired() {
        User user = new User("spotify123", "Test User", "test@example.com");
        assertTrue(user.isTokenExpired());
    }
}
