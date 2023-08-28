package br.com.igormartinez.virtualwallet.data.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferTransactionDTO(
    @NotNull(message = "The payer must be provided.")
    @Positive(message = "The payer must be a positive number.")
    Long payer,

    @NotNull(message = "The payee must be provided.")
    @Positive(message = "The payee must be a positive number.")
    Long payee,
    
    @NotNull(message = "The transaction value must be provided.")
    @Digits(integer = 10, fraction = 2, message = "The value must have up to 10 integer digits and 2 decimal digits of precision.")
    @Positive(message = "The value of transaction must be greater than zero.")
    BigDecimal value
) {}
