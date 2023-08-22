package br.com.igormartinez.virtualwallet.data.dto;

public record RegistrationDTO (
    String name, 
    String document,
    String email,
    String password
) {}
