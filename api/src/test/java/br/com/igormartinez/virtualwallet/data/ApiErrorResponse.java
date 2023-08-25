package br.com.igormartinez.virtualwallet.data;

import java.util.Map;

public record ApiErrorResponse(
    String type,
    String title,
    Integer status,
    String detail,
    String instance,
    Map<String, String> errors
) {}
