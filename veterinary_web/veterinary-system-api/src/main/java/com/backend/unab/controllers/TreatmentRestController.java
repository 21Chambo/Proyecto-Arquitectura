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

import com.backend.unab.dto.TreatmentDto;
import com.backend.unab.models.services.ITreatmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/treatments")
@Tag(name = "Treatments", description = "Treatment management endpoints")
public class TreatmentRestController {

	private final ITreatmentService treatmentService;

	public TreatmentRestController(ITreatmentService treatmentService) {
		this.treatmentService = treatmentService;
	}

	@PostMapping("")
	@Operation(summary = "Create a treatment")
	public ResponseEntity<TreatmentDto> create(@RequestBody TreatmentDto treatmentDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(treatmentService.create(treatmentDto));
	}

	@PutMapping("{id}")
	@Operation(summary = "Update a treatment")
	public ResponseEntity<TreatmentDto> update(@PathVariable Long id, @RequestBody TreatmentDto treatmentDto) {
		return ResponseEntity.ok(treatmentService.update(id, treatmentDto));
	}

	@DeleteMapping("{id}")
	@Operation(summary = "Delete a treatment")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		treatmentService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("{id}")
	@Operation(summary = "Find a treatment by id")
	public ResponseEntity<TreatmentDto> findById(@PathVariable Long id) {
		return ResponseEntity.ok(treatmentService.findById(id));
	}

	@GetMapping("")
	@Operation(summary = "List all treatments")
	public ResponseEntity<List<TreatmentDto>> findAll() {
		return ResponseEntity.ok(treatmentService.findAll());
	}
}
