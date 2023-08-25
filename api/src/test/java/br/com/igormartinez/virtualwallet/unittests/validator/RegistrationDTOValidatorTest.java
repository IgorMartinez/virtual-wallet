package br.com.igormartinez.virtualwallet.unittests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.igormartinez.virtualwallet.data.dto.RegistrationDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class RegistrationDTOValidatorTest {
    
    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSuccess() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "Name 1", "000.000.000-00", "email@email.com", "1234");
        
        Set<ConstraintViolation<RegistrationDTO>> violations = validator.validate(registrationDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testWithNameNull() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            null, "000.000.000-00", "email@email.com", "1234");
        
        Set<ConstraintViolation<RegistrationDTO>> violations = validator.validate(registrationDTO);
        assertEquals(1, violations.size());
        assertEquals("The name must be not blank.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithNameBlank() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "   ", "000.000.000-00", "email@email.com", "1234");
        
        Set<ConstraintViolation<RegistrationDTO>> violations = validator.validate(registrationDTO);
        assertEquals(1, violations.size());
        assertEquals("The name must be not blank.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithDocumentNull() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "Name 1", null, "email@email.com", "1234");
        
        Set<ConstraintViolation<RegistrationDTO>> violations = validator.validate(registrationDTO);
        assertEquals(1, violations.size());
        assertEquals("The document must be not blank.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithDocumentBlank() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "Name 1", "   ", "email@email.com", "1234");
        
        Set<ConstraintViolation<RegistrationDTO>> violations = validator.validate(registrationDTO);
        assertEquals(1, violations.size());
        assertEquals("The document must be not blank.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithEmailNull() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "Name 1", "000.000.000-00", null, "1234");
        
        Set<ConstraintViolation<RegistrationDTO>> violations = validator.validate(registrationDTO);
        assertEquals(1, violations.size());
        assertEquals("The email must be not blank.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithEmailBlank() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "Name 1", "000.000.000-00", "  ", "1234");
        
        Set<ConstraintViolation<RegistrationDTO>> violations = validator.validate(registrationDTO);
        assertEquals(1, violations.size());
        assertEquals("The email must be not blank.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithPasswordNull() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "Name 1", "000.000.000-00", "email@email.com", null);
        
        Set<ConstraintViolation<RegistrationDTO>> violations = validator.validate(registrationDTO);
        assertEquals(1, violations.size());
        assertEquals("The password must be not blank.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithPasswordBlank() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
            "Name 1", "000.000.000-00", "email@email.com", "  ");
        
        Set<ConstraintViolation<RegistrationDTO>> violations = validator.validate(registrationDTO);
        assertEquals(1, violations.size());
        assertEquals("The password must be not blank.", violations.iterator().next().getMessage());
    }
}
