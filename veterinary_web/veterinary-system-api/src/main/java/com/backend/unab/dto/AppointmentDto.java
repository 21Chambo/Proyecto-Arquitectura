package com.backend.unab.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AppointmentDto {

	private Long id;
	@NotNull @Positive
	private Long petId;
	private String petName;
	@NotNull @Positive
	private Long veterinarianId;
	private String veterinarianFullName;
	@NotNull
	private LocalDateTime appointmentDateTime;
	@NotBlank @Size(max = 250)
	private String reason;
	@NotBlank @Size(max = 30)
	private String status;
	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPetId() {
		return petId;
	}

	public void setPetId(Long petId) {
		this.petId = petId;
	}

	public String getPetName() {
		return petName;
	}

	public void setPetName(String petName) {
		this.petName = petName;
	}

	public Long getVeterinarianId() {
		return veterinarianId;
	}

	public void setVeterinarianId(Long veterinarianId) {
		this.veterinarianId = veterinarianId;
	}

	public String getVeterinarianFullName() {
		return veterinarianFullName;
	}

	public void setVeterinarianFullName(String veterinarianFullName) {
		this.veterinarianFullName = veterinarianFullName;
	}

	public LocalDateTime getAppointmentDateTime() {
		return appointmentDateTime;
	}

	public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
		this.appointmentDateTime = appointmentDateTime;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
