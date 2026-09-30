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

import com.backend.unab.dto.BreedDto;
import com.backend.unab.models.services.IBreedService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/breeds")
@Tag(name = "Breeds", description = "Breed management endpoints")
public class BreedRestController {

	private final IBreedService breedService;

	public BreedRestController(IBreedService breedService) {
		this.breedService = breedService;
	}

	@PostMapping("")
	@Operation(summary = "Create a breed")
	public ResponseEntity<BreedDto> create(@RequestBody BreedDto breedDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(breedService.create(breedDto));
	}

	@PutMapping("{id}")
	@Operation(summary = "Update a breed")
	public ResponseEntity<BreedDto> update(@PathVariable Long id, @RequestBody BreedDto breedDto) {
		return ResponseEntity.ok(breedService.update(id, breedDto));
	}

	@DeleteMapping("{id}")
	@Operation(summary = "Delete a breed")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		breedService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("{id}")
	@Operation(summary = "Find a breed by id")
	public ResponseEntity<BreedDto> findById(@PathVariable Long id) {
		return ResponseEntity.ok(breedService.findById(id));
	}

	@GetMapping("")
	@Operation(summary = "List all breeds")
	public ResponseEntity<List<BreedDto>> findAll() {
		return ResponseEntity.ok(breedService.findAll());
	}
}
