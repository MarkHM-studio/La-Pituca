package com.restobar.lapituca.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResetTokenResponse {
    private String message;
    private String correo;
    private String expiresAt;
}