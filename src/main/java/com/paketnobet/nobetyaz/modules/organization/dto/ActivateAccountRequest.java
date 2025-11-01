package com.paketnobet.nobetyaz.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivateAccountRequest(
        @NotBlank(message = "Kullanıcı adı zorunludur")
        String username,

        @NotBlank(message = "PIN zorunludur")
        String pin,

        @NotBlank(message = "Yeni şifre zorunludur")
        @Size(min = 6, max = 40, message = "Şifre 6-40 karakter arası olmalıdır")
        String newPassword
) {
}
