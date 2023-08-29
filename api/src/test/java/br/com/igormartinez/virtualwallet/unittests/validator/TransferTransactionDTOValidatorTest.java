package br.com.igormartinez.virtualwallet.unittests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.igormartinez.virtualwallet.data.dto.TransferTransactionDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class TransferTransactionDTOValidatorTest {
    
    private Validator validator;
    
    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSuccess() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(1L, 2L, new BigDecimal("0.01"));

        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertTrue(violations.isEmpty());
    }

    @Test 
    void testWithPayerNull() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(null, 2L, new BigDecimal("0.01"));
        
        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The payer must be provided.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithPayerNegative() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(-654L, 2L, new BigDecimal("0.01"));
        
        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The payer must be a positive number.", violations.iterator().next().getMessage());    
    }

    @Test
    void testWithPayerZero() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(0L, 2L, new BigDecimal("0.01"));
        
        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The payer must be a positive number.", violations.iterator().next().getMessage());    
    }

    @Test 
    void testWithPayeeNull() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(1L, null, new BigDecimal("0.01"));
        
        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The payee must be provided.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithPayeeNegative() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(1L, -444L, new BigDecimal("0.01"));
        
        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The payee must be a positive number.", violations.iterator().next().getMessage());    
    }

    @Test
    void testWithPayeeZero() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(1L, 0L, new BigDecimal("0.01"));
        
        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The payee must be a positive number.", violations.iterator().next().getMessage());    
    }

    @Test
    void testWithValueNull() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(1L, 2L, null);

        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The transaction value must be provided.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithValueWith11IntegerDigits() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(1L, 2L, new BigDecimal("12345678901.01"));

        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The value must have up to 10 integer digits and 2 decimal digits of precision.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithValueWith3IntegerDigits() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(1L, 2L, new BigDecimal("1234567890.015"));

        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The value must have up to 10 integer digits and 2 decimal digits of precision.", violations.iterator().next().getMessage());
    }

    @Test
    void testWithValueNegative() {
        TransferTransactionDTO transactionDTO 
            = new TransferTransactionDTO(1L, 2L, new BigDecimal("-1234567890.22"));

        Set<ConstraintViolation<TransferTransactionDTO>> violations = validator.validate(transactionDTO);
        assertEquals(1, violations.size());
        assertEquals("The value of transaction must be greater than zero.", violations.iterator().next().getMessage());
    }
}
