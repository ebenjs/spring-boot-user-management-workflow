package com.ebenjs.models.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String email;
    private String password;
    private String newPassword;
}
