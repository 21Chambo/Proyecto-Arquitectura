package com.backend.unab.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.backend.unab.exception.GlobalExceptionHandler;
import com.backend.unab.auth.JwtTokenService;
import com.backend.unab.models.entity.User;
import com.backend.unab.models.services.UsuarioService;

class AuthRestControllerTest {
    private AuthenticationManager authenticationManager;
    private UsuarioService users;
    private JwtTokenService tokens;
    private MockMvc mvc;
    private static final String LOGIN = "{\"username\":\"staff\",\"password\":\"Staff-Test-2026!\"}";

    @BeforeEach
    void setup() {
        authenticationManager = mock(AuthenticationManager.class);
        users = mock(UsuarioService.class);
        tokens = mock(JwtTokenService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuthRestController(authenticationManager, users, tokens))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void badCredentialsReturn401WithGenericMessage() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("internal details"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Usuario o contrasena invalidos."));
        verifyNoInteractions(users);
    }

    @Test
    void disabledAccountDoesNotRevealItsStatus() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("User is disabled"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario o contrasena invalidos."));
    }

    @Test
    void actualAuthenticationServiceFailureRemains500() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new InternalAuthenticationServiceException("Database unavailable"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void validLoginReturnsAccountAndRole() throws Exception {
        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(
                "staff", null, AuthorityUtils.createAuthorityList("ROLE_STAFF")));
        User user = new User();
        user.setUsername("staff");
        user.setFullName("Support Staff");
        when(users.findEntityByUsername("staff")).thenReturn(user);
        when(tokens.issue(eq("staff"), eq("Support Staff"), any())).thenReturn("signed-jwt");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("staff"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_STAFF"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("signed-jwt"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
