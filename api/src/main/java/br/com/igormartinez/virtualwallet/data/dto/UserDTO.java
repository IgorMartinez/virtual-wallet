package br.com.igormartinez.virtualwallet.data.dto;

public record UserDTO(
    Long id,
    String name,
    String document,
    String email,
    String role
) {}
