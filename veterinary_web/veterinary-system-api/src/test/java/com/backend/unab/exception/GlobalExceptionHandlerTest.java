package com.backend.unab.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.backend.unab.dto.ApiErrorDto;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private GlobalExceptionHandler handler;

	@Test
	void shouldIdentifyRelatedPetsWhenBreedDeleteConflicts() {
		when(request.getMethod()).thenReturn("DELETE");
		when(request.getRequestURI()).thenReturn("/api/breeds/1");

		ResponseEntity<ApiErrorDto> response = handler.handleDataIntegrityViolation(
				new DataIntegrityViolationException("constraint violation", new SQLException("fk_pet_breed")), request);

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertEquals("No se puede eliminar la raza porque esta asignada a mascotas.", response.getBody().getMessage());
	}

	@Test
	void shouldUseGenericMessageForUnrecognizedDeleteConstraint() {
		when(request.getMethod()).thenReturn("DELETE");
		when(request.getRequestURI()).thenReturn("/api/resources/1");

		ResponseEntity<ApiErrorDto> response = handler.handleDataIntegrityViolation(
				new DataIntegrityViolationException("database constraint details"), request);

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertEquals(
				"No se puede eliminar este registro porque tiene datos relacionados. "
						+ "Elimina primero los registros dependientes y vuelve a intentarlo.",
				response.getBody().getMessage());
	}

	@Test
	void shouldReturnConflictForOtherDataIntegrityViolations() {
		when(request.getMethod()).thenReturn("POST");
		when(request.getRequestURI()).thenReturn("/api/customers");

		ResponseEntity<ApiErrorDto> response = handler.handleDataIntegrityViolation(
				new DataIntegrityViolationException("database constraint details"), request);

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertEquals("La operacion entra en conflicto con datos existentes.", response.getBody().getMessage());
	}
}