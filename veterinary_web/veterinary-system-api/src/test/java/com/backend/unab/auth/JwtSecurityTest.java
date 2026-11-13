package com.backend.unab.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.security.SecureRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import com.backend.unab.controllers.*;
import com.backend.unab.exception.GlobalExceptionHandler;
import com.backend.unab.models.entity.User;
import com.backend.unab.models.services.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {JwtSecurityTest.TestConfiguration.class, SpringSecurityConfig.class,
        JwtConfiguration.class, JwtTokenService.class})
class JwtSecurityTest {
    @Configuration @EnableWebMvc @EnableWebSecurity
    @Import({AuthRestController.class, PetRestController.class, GlobalExceptionHandler.class})
    static class TestConfiguration {
        @Bean UsuarioService profiles(PasswordEncoder encoder) {
            UsuarioService users = mock(UsuarioService.class);
            User staff = new User(); staff.setUsername("staff"); staff.setFullName("Support Staff");
            when(users.findEntityByUsername("staff")).thenReturn(staff);
            when(users.loadUserByUsername("staff")).thenReturn(org.springframework.security.core.userdetails.User
                    .withUsername("staff").password(encoder.encode("test-only-password")).roles("STAFF").build());
            return users;
        }
        @Bean IPetService pets() { return mock(IPetService.class); }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        registry.add("APP_JWT_SECRET", () -> Base64.getEncoder().encodeToString(bytes));
        registry.add("APP_JWT_TTL_SECONDS", () -> "900");
        registry.add("app.cors.allowed-origins", () -> "http://localhost:3000");
    }

    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy filters;
    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;
    @Autowired JwtTokenService tokens;
    private MockMvc mvc;

    @BeforeEach void setup() { mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(filters).build(); }
    private String token() { return tokens.issue("staff", "Support Staff", List.of("ROLE_STAFF")); }

    @Test void signedTokenContainsNoPasswordAndHasExpiry() {
        String token = token();
        Jwt jwt = decoder.decode(token);
        assertEquals("staff", jwt.getSubject());
        assertEquals(List.of("ROLE_STAFF"), jwt.getClaimAsStringList("roles"));
        assertEquals(900, jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
        assertFalse(jwt.getClaims().toString().contains("test-only-password"));
        assertFalse(jwt.getClaims().containsKey("password"));
        assertEquals(3, token.split("\\.").length);
    }

    @Test void validBearerTokenAllowsAccess() throws Exception {
        mvc.perform(get("/api/pets").header("Authorization", "Bearer " + token())).andExpect(status().isOk());
    }

    @Test void expiredTokenReturns401() throws Exception {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(JwtConfiguration.ISSUER).subject("staff")
                .issuedAt(now.minusSeconds(3600)).expiresAt(now.minusSeconds(2)).build();
        String expired = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        mvc.perform(get("/api/pets").header("Authorization", "Bearer " + expired)).andExpect(status().isUnauthorized());
    }

    @Test void tamperedTokenAndBasicCredentialsAreRejected() throws Exception {
        String token = token();
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        mvc.perform(get("/api/pets").header("Authorization", "Bearer " + tampered)).andExpect(status().isUnauthorized());
        String basic = Base64.getEncoder().encodeToString("staff:test-only-password".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        mvc.perform(get("/api/pets").header("Authorization", "Basic " + basic)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pets")).andExpect(status().isUnauthorized());
    }

    @Test void missingPetNameReturns400WithFieldAndNeverPersists() throws Exception {
        mvc.perform(post("/api/pets").header("Authorization", "Bearer " + token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"customerId\":1,\"breedId\":1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.name").exists());
        verify(context.getBean(IPetService.class), never()).create(any());
    }

    @Test void wrongLoginAndMissingCredentialsHaveExplicitErrors() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"staff\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.username").exists());
    }

    @Test void portalOriginCanLoginWhileUntrustedOriginsAreRejected() throws Exception {
        String login = "{\"username\":\"staff\",\"password\":\"test-only-password\"}";
        mvc.perform(post("/api/auth/login").header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        mvc.perform(post("/api/auth/login").header("Origin", "https://untrusted.example")
                .contentType(MediaType.APPLICATION_JSON).content(login)).andExpect(status().isForbidden());
    }

    @Test void missingOrWeakSigningKeyFailsClosed() {
        JwtConfiguration configuration = new JwtConfiguration();
        assertThrows(IllegalStateException.class, () -> configuration.jwtSigningKey(""));
        assertThrows(IllegalStateException.class, () -> configuration.jwtSigningKey("not-base64!"));
        assertThrows(IllegalStateException.class, () -> configuration.jwtSigningKey(Base64.getEncoder().encodeToString(new byte[16])));
    }
}
