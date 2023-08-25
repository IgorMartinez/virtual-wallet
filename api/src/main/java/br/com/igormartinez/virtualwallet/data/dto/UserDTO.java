package br.com.igormartinez.virtualwallet.data.dto;

import java.math.BigDecimal;

public record UserDTO(
    Long id,
    String name,
    String document,
    String email,
    BigDecimal accountBalance,
    String role
) {}
