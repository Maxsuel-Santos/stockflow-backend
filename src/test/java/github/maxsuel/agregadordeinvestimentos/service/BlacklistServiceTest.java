package github.maxsuel.agregadordeinvestimentos.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Tests configs
@DisplayName("Tests for Blacklist Service")
class BlacklistServiceTest {

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private CacheManager cacheManager;

    @Nested
    @DisplayName("Token Blacklisting.")
    public class TokenBlacklistingTests {

        @Test
        @DisplayName("Should add token to cache when blacklisted.")
        public void shouldAddTokenToCache() {
            // Arrange
            String token = "sample-jwt-token-123";

            // Act
            blacklistService.blacklistToken(token);

            // Assert
            var cache = cacheManager.getCache("invalidTokens");
            assertNotNull(cache, "Cache 'invalidTokens' should exist");
            assertEquals(token, Objects.requireNonNull(cache.get(token)).get());
        }

    }

    @Nested
    @DisplayName("Token Retrieval.")
    public class TokenRetrievalTests {

        @Test
        @DisplayName("Should return null and trigger cacheable annotation.")
        public void shouldReturnNullWhenCheckingToken() {
            // Arrange
            String token = "another-token-456";
            blacklistService.blacklistToken(token);

            // Act
            String cachedToken = blacklistService.getBlacklistedToken(token);

            // Assert
            assertNotNull(cachedToken);
            assertEquals(token, cachedToken);
        }

        @Test
        @DisplayName("Should return null if token is not in blacklist.")
        public void shouldReturnNullIfTokenNotFound() {
            // Act
            String result = blacklistService.getBlacklistedToken("non-existent-token");

            // Assert
            assertNull(result);
        }

    }

}
