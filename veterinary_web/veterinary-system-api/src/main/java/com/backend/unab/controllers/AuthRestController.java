package com.backend.unab.controllers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.unab.dto.AuthLoginRequestDto;
import com.backend.unab.dto.AuthLoginResponseDto;
import com.backend.unab.models.entity.User;
import com.backend.unab.models.services.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthRestController.class);

	private final AuthenticationManager authenticationManager;
	private final UsuarioService usuarioService;

	public AuthRestController(AuthenticationManager authenticationManager, UsuarioService usuarioService) {
		this.authenticationManager = authenticationManager;
		this.usuarioService = usuarioService;
	}

	@PostMapping("/login")
	@Operation(summary = "Authenticate a user and return a Basic Auth token")
	@SecurityRequirements
	public ResponseEntity<AuthLoginResponseDto> login(@RequestBody AuthLoginRequestDto request) {
		if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
			LOGGER.warn("Rejected authentication request because required credentials were missing");
			throw new IllegalArgumentException("Username and password are required");
		}

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword()));

		List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		User user = usuarioService.findEntityByUsername(request.getUsername().trim());

		String rawToken = request.getUsername().trim() + ":" + request.getPassword();
		String accessToken = "Basic " + Base64.getEncoder().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));

		AuthLoginResponseDto response = new AuthLoginResponseDto();
		response.setUsername(user.getUsername());
		response.setFullName(user.getFullName());
		response.setRoles(roles);
		response.setTokenType("Basic");
		response.setAccessToken(accessToken);

		LOGGER.info("Authentication succeeded");
		return ResponseEntity.ok(response);
	}
}
