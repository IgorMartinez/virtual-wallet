package br.com.igormartinez.virtualwallet.unittests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.igormartinez.virtualwallet.data.dto.PersonalTransactionDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class PersonalTransactionDTOValidatorTest {

    private Validator validator;
    
    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSuccess() {
        PersonalTransactionDTO transactionDTO = new PersonalTransactionDTO(1L, new BigDecimal("0.01"));
    
        Set<ConstraintViolation<PersonalTransactionDTO>> violations = validator.validate(transactionDTO);
        assertTrue(violations.isEmpty());
    }

    @Test 
    void testWithUserNull() {
        PersonalTransactionDTO transactionDTO = new PersonalTransactionDTO(null, new BigDecimal("0.01"));

        Set<ConstraintViolation<PersonalTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The user must be provided.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithUserNegative() {
        PersonalTransactionDTO transactionDTO = new PersonalTransactionDTO(-123L, new BigDecimal("0.01"));

        Set<ConstraintViolation<PersonalTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The user must be a positive number.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithUserZero() {
        PersonalTransactionDTO transactionDTO = new PersonalTransactionDTO(0L, new BigDecimal("0.01"));

        Set<ConstraintViolation<PersonalTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The user must be a positive number.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithValueNull() {
        PersonalTransactionDTO transactionDTO = new PersonalTransactionDTO(1L, null);

        Set<ConstraintViolation<PersonalTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The transaction value must be provided.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithValueWith11IntegerDigits() {
        PersonalTransactionDTO transactionDTO = 
            new PersonalTransactionDTO(1L, new BigDecimal("12345678901.01"));

        Set<ConstraintViolation<PersonalTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The value must have up to 10 integer digits and 2 decimal digits of precision.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithValueWith3IntegerDigits() {
        PersonalTransactionDTO transactionDTO = 
            new PersonalTransactionDTO(1L, new BigDecimal("1234567890.012"));

        Set<ConstraintViolation<PersonalTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The value must have up to 10 integer digits and 2 decimal digits of precision.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithValueNegative() {
        PersonalTransactionDTO transactionDTO = 
            new PersonalTransactionDTO(1L, new BigDecimal("-1234567890.01"));

        Set<ConstraintViolation<PersonalTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The value of transaction must be greater than zero.", violations.iterator().next().getMessage());
    }
}
