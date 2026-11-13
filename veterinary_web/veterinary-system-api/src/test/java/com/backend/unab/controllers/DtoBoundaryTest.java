package com.backend.unab.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.backend.unab.exception.GlobalExceptionHandler;
import com.backend.unab.models.services.*;

class DtoBoundaryTest {
    static Stream<Arguments> invalidPayloads() {
        return Stream.of(
            Arguments.of(new SpeciesRestController(mock(ISpeciesService.class)), "/api/species", "{}", "name"),
            Arguments.of(new BreedRestController(mock(IBreedService.class)), "/api/breeds", "{}", "speciesId"),
            Arguments.of(new CustomerRestController(mock(ICustomerService.class)), "/api/customers", "{}", "fullName"),
            Arguments.of(new PetRestController(mock(IPetService.class)), "/api/pets", "{}", "name"),
            Arguments.of(new VeterinarianRestController(mock(IVeterinarianService.class)), "/api/veterinarians", "{}", "documentNumber"),
            Arguments.of(new AppointmentRestController(mock(IAppointmentService.class)), "/api/appointments", "{}", "appointmentDateTime"),
            Arguments.of(new MedicalRecordRestController(mock(IMedicalRecordService.class)), "/api/medical-records", "{}", "diagnosis"),
            Arguments.of(new TreatmentRestController(mock(ITreatmentService.class)), "/api/treatments", "{}", "description"),
            Arguments.of(new PetRestController(mock(IPetService.class)), "/api/pets",
                "{\"customerId\":1,\"breedId\":1,\"name\":\"" + "a".repeat(101) + "\"}", "name"),
            Arguments.of(new CustomerRestController(mock(ICustomerService.class)), "/api/customers",
                "{\"fullName\":\"Test\",\"documentNumber\":\"Test\",\"email\":\"invalid-email\"}", "email")
        );
    }

    @ParameterizedTest @MethodSource("invalidPayloads")
    void invalidInputReturns400AndNamesTheField(Object controller, String path, String json, String field) throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors." + field).exists());
    }
}
