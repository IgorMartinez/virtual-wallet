package br.com.igormartinez.virtualwallet.data.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrationDTO (
    @NotBlank(message = "The name must be not blank") 
    String name, 

    @NotBlank(message = "The document must be not blank")
    String document,
    
    @NotBlank(message = "The email must be not blank")
    String email,
    
    @NotBlank(message = "The password must be not blank")
    String password
) {}
