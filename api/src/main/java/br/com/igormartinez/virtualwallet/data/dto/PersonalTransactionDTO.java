package br.com.igormartinez.virtualwallet.data.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PersonalTransactionDTO(
    @NotNull(message = "The user must be provided.")
    @Positive(message = "The user must be a positive number.")
    Long user,

    @NotNull(message = "The transaction value must be provided.")
    @Digits(integer = 12, fraction = 2, message = "The value must have 12 integer digits and 2 decimal digits.")
    @Positive(message = "The value of transaction must be greater than zero.")
    BigDecimal value
) {}
