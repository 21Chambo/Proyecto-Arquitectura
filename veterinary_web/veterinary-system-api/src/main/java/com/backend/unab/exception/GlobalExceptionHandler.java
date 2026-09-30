package com.backend.unab.exception;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.backend.unab.dto.ApiErrorDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorDto> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		LOGGER.warn("Resource not found for path {}: {}", request.getRequestURI(), ex.getMessage());
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorDto> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		LOGGER.warn("Invalid request for path {}: {}", request.getRequestURI(), ex.getMessage());
		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorDto> handleDataIntegrityViolation(
			DataIntegrityViolationException ex, HttpServletRequest request) {
		String message = "DELETE".equalsIgnoreCase(request.getMethod())
				? resolveDeleteConflictMessage(ex)
				: "La operacion entra en conflicto con datos existentes.";
		LOGGER.warn("Data integrity conflict for {} {}", request.getMethod(), request.getRequestURI());
		return buildResponse(HttpStatus.CONFLICT, message, request.getRequestURI());
	}

	private String resolveDeleteConflictMessage(DataIntegrityViolationException ex) {
		String causeMessage = ex.getMostSpecificCause().getMessage();
		if (causeMessage != null) {
			if (causeMessage.contains("fk_breed_species")) {
				return "No se puede eliminar la especie porque tiene razas asociadas.";
			}
			if (causeMessage.contains("fk_pet_customer")) {
				return "No se puede eliminar el cliente porque tiene mascotas asociadas.";
			}
			if (causeMessage.contains("fk_pet_breed")) {
				return "No se puede eliminar la raza porque esta asignada a mascotas.";
			}
			if (causeMessage.contains("fk_appointment_pet")) {
				return "No se puede eliminar la mascota porque tiene citas asociadas.";
			}
			if (causeMessage.contains("fk_appointment_veterinarian")) {
				return "No se puede eliminar el veterinario porque tiene citas asociadas.";
			}
			if (causeMessage.contains("fk_medical_record_appointment")) {
				return "No se puede eliminar la cita porque tiene una historia clinica asociada.";
			}
			if (causeMessage.contains("fk_treatment_medical_record")) {
				return "No se puede eliminar la historia clinica porque tiene tratamientos asociados.";
			}
		}
		return "No se puede eliminar este registro porque tiene datos relacionados. "
				+ "Elimina primero los registros dependientes y vuelve a intentarlo.";
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorDto> handleUnexpected(Exception ex, HttpServletRequest request) {
		LOGGER.error("Unexpected error for path {}", request.getRequestURI(), ex);
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
	}

	private ResponseEntity<ApiErrorDto> buildResponse(HttpStatus status, String message, String path) {
		ApiErrorDto error = new ApiErrorDto(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
		return ResponseEntity.status(status).body(error);
	}
}
