package com.ebenjs.controllers;

import com.ebenjs.enums.ApiResponseStatus;
import com.ebenjs.models.requests.LoginRequest;
import com.ebenjs.models.requests.RegisterRequest;
import com.ebenjs.models.responses.AuthenticationResponse;
import com.ebenjs.services.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public AuthenticationResponse register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthenticationResponse authenticationResponse = authenticationService.register(request);
        Cookie cookie = new Cookie("jwtToken", authenticationResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);
        return authenticationResponse;
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        System.out.println("Login request ::"+request.toString());
        AuthenticationResponse authenticationResponse = authenticationService.login(request);
        Cookie cookie = new Cookie("jwtToken", authenticationResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);
        return authenticationResponse;
    }

    @GetMapping("/validate")
    public AuthenticationResponse validateTokenSentThroughCookie(@CookieValue(value = "jwtToken") String jwtToken){
        return authenticationService.validate(jwtToken);
    }

    @GetMapping("/confirmation")
    public RedirectView activateUserAccount(@RequestParam String hash){
        AuthenticationResponse authenticationResponse = authenticationService.activateUserAccount(hash);
        if(authenticationResponse.getStatus() == ApiResponseStatus.ERROR){
            System.out.println("Error");
        }
        return new RedirectView(authenticationResponse.getRedirectionUrl());
    }
}
