package com.backend.unab.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthLoginRequestDto {

	@NotBlank @Size(max = 50)
	private String username;
	@NotBlank @Size(max = 256)
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
