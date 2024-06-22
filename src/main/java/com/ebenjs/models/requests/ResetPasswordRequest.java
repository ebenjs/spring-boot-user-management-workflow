package com.ebenjs.models.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String hash;
    private String email;
    private String newPassword;
}
