package com.backend.unab.controllers;

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
import com.backend.unab.auth.JwtTokenService;
import jakarta.validation.Valid;
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
	private final JwtTokenService tokens;

	public AuthRestController(AuthenticationManager authenticationManager, UsuarioService usuarioService, JwtTokenService tokens) {
		this.authenticationManager = authenticationManager;
		this.usuarioService = usuarioService;
		this.tokens = tokens;
	}

	@PostMapping("/login")
	@Operation(summary = "Authenticate a user and return an expiring signed JWT")
	@SecurityRequirements
	public ResponseEntity<AuthLoginResponseDto> login(@Valid @RequestBody AuthLoginRequestDto request) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword()));

		List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		User user = usuarioService.findEntityByUsername(request.getUsername().trim());

		String accessToken = tokens.issue(user.getUsername(), user.getFullName(), roles);

		AuthLoginResponseDto response = new AuthLoginResponseDto();
		response.setUsername(user.getUsername());
		response.setFullName(user.getFullName());
		response.setRoles(roles);
		response.setTokenType("Bearer");
		response.setAccessToken(accessToken);

		LOGGER.info("Authentication succeeded");
		return ResponseEntity.ok(response);
	}
}
