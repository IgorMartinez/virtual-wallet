package br.com.igormartinez.virtualwallet.data.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record TransactionDTO (
    Long id,
    String type,
    BigDecimal value,
    ZonedDateTime datetime
) {}
