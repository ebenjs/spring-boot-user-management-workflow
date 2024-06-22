package com.ebenjs.models.responses;

import com.ebenjs.entities.User;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class AuthenticationResponse extends BaseApiResponse<User>{
    private String token;
    private String redirectionUrl;
}

