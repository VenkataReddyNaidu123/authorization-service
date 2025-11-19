package Logistics.App.service;

import Logistics.App.entity.RefreshToken;
import Logistics.App.entity.User;
import Logistics.App.repository.RefreshTokenRepository;
import Logistics.App.repository.AuthorizationRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthorizationRepo authorizationRepo;
    private final long refreshExpirationMs;

    public TokenService(RefreshTokenRepository refreshTokenRepository,
                        AuthorizationRepo authorizationRepo,
                        @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.authorizationRepo = authorizationRepo;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setToken(UUID.randomUUID().toString()); // secure random token
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isValid(RefreshToken token) {
        return token != null && !token.isRevoked() && token.getExpiryDate().isAfter(Instant.now());
    }

    public RefreshToken rotate(RefreshToken existing) {

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);
        return createRefreshToken(existing.getUser());
    }

    public void revokeRefreshToken(RefreshToken token) {
        if (token == null) return;
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void revokeAllForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
