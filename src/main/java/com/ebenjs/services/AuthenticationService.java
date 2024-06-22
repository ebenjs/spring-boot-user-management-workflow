package com.ebenjs.services;

import com.ebenjs.entities.User;
import com.ebenjs.enums.ApiResponseStatus;
import com.ebenjs.enums.Role;
import com.ebenjs.models.requests.LoginRequest;
import com.ebenjs.models.requests.RegisterRequest;
import com.ebenjs.models.responses.AuthenticationResponse;
import com.ebenjs.repositories.UserRepository;
import com.ebenjs.security.JwtService;
import com.ebenjs.services.mail.MailSendingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {

    @Value("${app.base.url}")
    private String appBaseUrl;

    @Value("${app.base.front.url}")
    private String appBaseFrontUrl;

    @Value("${app.api.prefix}")
    private String apiSuffix;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final MailSendingService mailSendingService;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, UserDetailsService userDetailsService, MailSendingService mailSendingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.mailSendingService = mailSendingService;
    }

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        User user = User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .build();

        Optional<User> retrievedUser = userRepository.getUserByEmail(user.getEmail());

        if (retrievedUser.isPresent()) {
            return AuthenticationResponse.builder()
                    .status(ApiResponseStatus.ERROR)
                    .message("User already exists")
                    .httpCode(HttpStatus.CONFLICT.value())
                    .build();
        }

        user.setActivationHash(generateActivationHash());
        userRepository.save(user);

        sendConfirmationLink(user.getEmail(), user.getFirstname(), user.getActivationHash());

        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .status(ApiResponseStatus.SUCCESS)
                .message("Registration succeeded")
                .token(jwtToken)
                .data(user)
                .httpCode(HttpStatus.CREATED.value())
                .build();
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Optional<User> user = userRepository.getUserByEmail(loginRequest.getUsername());

        if (user.isEmpty()) {
            return AuthenticationResponse.builder()
                    .status(ApiResponseStatus.ERROR)
                    .message("This user does not exist")
                    .httpCode(HttpStatus.NOT_FOUND.value())
                    .build();
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        String jwtToken = jwtService.generateToken(user.get());
        return AuthenticationResponse.builder()
                .status(ApiResponseStatus.SUCCESS)
                .message("Login successful")
                .token(jwtToken)
                .data(user.get())
                .httpCode(HttpStatus.OK.value())
                .build();
    }

    public AuthenticationResponse validate(String token) {
        final String username = jwtService.extractUsername(token);
        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        if (token != null && jwtService.isTokenValid(token, userDetails)) {

            User user = userRepository.getUserByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return AuthenticationResponse.builder()
                    .status(ApiResponseStatus.SUCCESS)
                    .message("Automatic login success")
                    .data(user)
                    .httpCode(HttpStatus.OK.value())
                    .build();
        }

        return AuthenticationResponse.builder()
                .status(ApiResponseStatus.ERROR)
                .message("Automatic login error")
                .httpCode(HttpStatus.UNAUTHORIZED.value())
                .build();
    }

    public AuthenticationResponse activateUserAccount(String hash) {
        Optional<User> user = userRepository.getUserByActivationHash(hash);
        if (user.isEmpty()) {
            return AuthenticationResponse.builder()
                    .status(ApiResponseStatus.ERROR)
                    .httpCode(HttpStatus.NOT_FOUND.value())
                    .message("The activation is broken")
                    .redirectionUrl(appBaseFrontUrl + "confirmation?status=" + 0)
                    .build();
        }

        user.get().setActivationHash("");
        user.get().setEnabled(true);
        userRepository.save(user.get());

        return AuthenticationResponse.builder()
                .status(ApiResponseStatus.SUCCESS)
                .httpCode(HttpStatus.OK.value())
                .message("Account activated")
                .redirectionUrl(appBaseFrontUrl + "confirmation?status=" + 1)
                .build();
    }

    private void sendConfirmationLink(String email, String name, String activationHash) {
        Context thymeleafContext = new Context();
        String generatedActivationLink = generateActivationLink(activationHash);
        thymeleafContext.setVariable("name", name);
        thymeleafContext.setVariable("url", generatedActivationLink);
        mailSendingService.sendHtmlWithTemplateToOne("user-c7c01a93515e3166@smtp.tickpluswise.com", email, "Testing", thymeleafContext, "emails/account-activation.html");
    }

    private String generateActivationLink(String activationHash) {
        return appBaseUrl + apiSuffix + "auth/confirmation?hash=" + activationHash;
    }

    private String generateActivationHash() {
        return UUID.randomUUID().toString();
    }
}