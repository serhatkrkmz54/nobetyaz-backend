package com.paketnobet.nobetyaz.modules.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Mevcut şifre boş olamaz")
        String currentPassword,

        @NotBlank(message = "Yeni şifre boş olamaz")
        @Size(min = 6, max = 40, message = "Yeni şifre en az 6, en fazla 40 karakter olmalıdır")
        String newPassword
) {
}
