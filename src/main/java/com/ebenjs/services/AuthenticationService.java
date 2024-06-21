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
            AuthenticationResponse authenticationResponse = new AuthenticationResponse();
            authenticationResponse.setStatus(ApiResponseStatus.ERROR);
            authenticationResponse.setMessage("User already exists");
            authenticationResponse.setHttpCode(HttpStatus.CONFLICT.value());
            return authenticationResponse;
        }

        user.setActivationHash(generateActivationHash());
        userRepository.save(user);

        sendConfirmationLink(user.getEmail(), user.getFirstname(), user.getActivationHash());

        String jwtToken = jwtService.generateToken(user);
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setStatus(ApiResponseStatus.SUCCESS);
        authenticationResponse.setMessage("Registration succeeded");
        authenticationResponse.setToken(jwtToken);
        authenticationResponse.setData(user);
        authenticationResponse.setHttpCode(HttpStatus.CREATED.value());
        return authenticationResponse;
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        System.out.println("Step 1");
        Optional<User> user = userRepository.getUserByEmail(loginRequest.getUsername());

        if (user.isEmpty()){
            System.out.println("It is empty");
            AuthenticationResponse authenticationResponse = new AuthenticationResponse();
            authenticationResponse.setStatus(ApiResponseStatus.ERROR);
            authenticationResponse.setMessage("This user does not exist");
            authenticationResponse.setHttpCode(HttpStatus.NOT_FOUND.value());
            return authenticationResponse;
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        System.out.println("Step 2");

        System.out.println("It is NOT empty");

        String jwtToken = jwtService.generateToken(user.get());
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setHttpCode(HttpStatus.OK.value());
        authenticationResponse.setStatus(ApiResponseStatus.SUCCESS);
        authenticationResponse.setToken(jwtToken);
        authenticationResponse.setData(user.get());
        authenticationResponse.setMessage("Login successfull");
        return authenticationResponse;
    }

    public AuthenticationResponse validate(String token) {
        final String username = jwtService.extractUsername(token);
        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        if (token != null && jwtService.isTokenValid(token, userDetails)) {

            User user = userRepository.getUserByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            authenticationResponse.setStatus(ApiResponseStatus.SUCCESS);
            authenticationResponse.setMessage("Automatic login success");
            authenticationResponse.setData(user);
            authenticationResponse.setHttpCode(HttpStatus.OK.value());
            return authenticationResponse;
        }

        authenticationResponse.setStatus(ApiResponseStatus.ERROR);
        authenticationResponse.setMessage("Automatic login error");
        authenticationResponse.setHttpCode(HttpStatus.UNAUTHORIZED.value());
        return authenticationResponse;
    }

    public AuthenticationResponse activateUserAccount(String hash) {
        Optional<User> user = userRepository.getUserByActivationHash(hash);
        if(user.isEmpty()){
            AuthenticationResponse authenticationResponse = new AuthenticationResponse();
            authenticationResponse.setStatus(ApiResponseStatus.ERROR);
            authenticationResponse.setHttpCode(HttpStatus.NOT_FOUND.value());
            authenticationResponse.setMessage("The activation is broken");
            authenticationResponse.setRedirectionUrl(appBaseFrontUrl+"confirmation?status="+0);
            return authenticationResponse;
        }

        user.get().setActivationHash("");
        user.get().setEnabled(true);
        userRepository.save(user.get());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setStatus(ApiResponseStatus.SUCCESS);
        authenticationResponse.setHttpCode(HttpStatus.OK.value());
        authenticationResponse.setMessage("Account activated");
        authenticationResponse.setRedirectionUrl(appBaseFrontUrl+"confirmation?status="+1);
        return authenticationResponse;
    }

    private void sendConfirmationLink(String email, String name, String activationHash) {
        Context thymeleafContext = new Context();
        String generatedActivationLink = generateActivationLink(activationHash);
        thymeleafContext.setVariable("name", name);
        thymeleafContext.setVariable("url", generatedActivationLink);
        mailSendingService.sendHtmlWithTemplateToOne("user-c7c01a93515e3166@smtp.tickpluswise.com", email, "Testing", thymeleafContext,"emails/account-activation.html");
    }

    private String generateActivationLink(String activationHash) {
        return appBaseUrl+apiSuffix+"auth/confirmation?hash="+activationHash;
    }

    private String generateActivationHash(){
        return UUID.randomUUID().toString();
    }
}
