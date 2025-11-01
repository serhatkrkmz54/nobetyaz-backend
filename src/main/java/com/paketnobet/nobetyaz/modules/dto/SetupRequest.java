package com.paketnobet.nobetyaz.modules.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SetupRequest {

    @NotBlank @Size(min = 3, max = 50)
    private String industryProfile; // HASTANE, GUVENLIK, ASKERIYE VS.

    @NotBlank @Size(min = 3, max = 20)
    private String adminUsername;

    @NotBlank @Size(max = 50) @Email
    private String adminEmail;

    @NotBlank @Size(min = 6, max = 40)
    private String adminPassword;

    @NotBlank
    private String adminFirstName;

    @NotBlank
    private String adminLastName;

    private String adminPhoneNumber;
}
