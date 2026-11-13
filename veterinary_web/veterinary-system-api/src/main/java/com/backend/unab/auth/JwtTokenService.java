package com.backend.unab.auth;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final JwtEncoder encoder;
    private final long ttlSeconds;

    public JwtTokenService(JwtEncoder encoder, @Value("${APP_JWT_TTL_SECONDS:900}") long ttlSeconds) {
        if (ttlSeconds < 60 || ttlSeconds > 3600) {
            throw new IllegalStateException("APP_JWT_TTL_SECONDS must be between 60 and 3600.");
        }
        this.encoder = encoder;
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(String username, String fullName, List<String> roles) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(JwtConfiguration.ISSUER)
                .subject(username).issuedAt(now).expiresAt(now.plusSeconds(ttlSeconds))
                .id(UUID.randomUUID().toString()).claim("roles", roles).claim("fullName", fullName).build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
