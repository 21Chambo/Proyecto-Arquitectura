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

import com.backend.unab.dto.PetDto;
import com.backend.unab.models.services.IPetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pets", description = "Pet management endpoints")
public class PetRestController {

	private final IPetService petService;

	public PetRestController(IPetService petService) {
		this.petService = petService;
	}

	@PostMapping("")
	@Operation(summary = "Create a pet")
	public ResponseEntity<PetDto> create(@RequestBody PetDto petDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(petService.create(petDto));
	}

	@PutMapping("{id}")
	@Operation(summary = "Update a pet")
	public ResponseEntity<PetDto> update(@PathVariable Long id, @RequestBody PetDto petDto) {
		return ResponseEntity.ok(petService.update(id, petDto));
	}

	@DeleteMapping("{id}")
	@Operation(summary = "Delete a pet")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		petService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("{id}")
	@Operation(summary = "Find a pet by id")
	public ResponseEntity<PetDto> findById(@PathVariable Long id) {
		return ResponseEntity.ok(petService.findById(id));
	}

	@GetMapping("")
	@Operation(summary = "List all pets")
	public ResponseEntity<List<PetDto>> findAll() {
		return ResponseEntity.ok(petService.findAll());
	}
}
