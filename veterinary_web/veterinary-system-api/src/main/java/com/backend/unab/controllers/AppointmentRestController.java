package com.backend.unab.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.unab.dto.AppointmentDto;
import com.backend.unab.models.services.IAppointmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointments", description = "Appointment management endpoints")
public class AppointmentRestController {

	private final IAppointmentService appointmentService;

	public AppointmentRestController(IAppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@PostMapping("")
	@Operation(summary = "Create an appointment")
	public ResponseEntity<AppointmentDto> create(@RequestBody AppointmentDto appointmentDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(appointmentDto));
	}

	@PutMapping("{id}")
	@Operation(summary = "Update an appointment")
	public ResponseEntity<AppointmentDto> update(@PathVariable Long id, @RequestBody AppointmentDto appointmentDto) {
		return ResponseEntity.ok(appointmentService.update(id, appointmentDto));
	}

	@DeleteMapping("{id}")
	@Operation(summary = "Delete an appointment")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		appointmentService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("{id}")
	@Operation(summary = "Find an appointment by id")
	public ResponseEntity<AppointmentDto> findById(@PathVariable Long id) {
		return ResponseEntity.ok(appointmentService.findById(id));
	}

	@GetMapping("")
	@Operation(summary = "List all appointments")
	public ResponseEntity<List<AppointmentDto>> findAll() {
		return ResponseEntity.ok(appointmentService.findAll());
	}
}
