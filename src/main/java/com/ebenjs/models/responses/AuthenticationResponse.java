package com.ebenjs.models.responses;

import com.ebenjs.entities.User;
import lombok.Data;

@Data
public class AuthenticationResponse extends BaseApiResponse<User>{
    private String token;
    private String redirectionUrl;
}
