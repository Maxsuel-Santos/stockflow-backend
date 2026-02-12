package github.maxsuel.agregadordeinvestimentos.service;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BlacklistService {

    @CachePut(value = "invalidTokens", key = "#token")
    public String blacklistToken(String token) {
        return token;
    }

    @Cacheable(value = "invalidTokens", key = "#token")
    public String getBlacklistedToken(String token) {
        return null;
    }

}
