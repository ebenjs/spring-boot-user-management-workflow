package com.ebenjs.services;

import com.ebenjs.entities.User;
import com.ebenjs.enums.ApiResponseStatus;
import com.ebenjs.models.requests.ForgotPasswordRequest;
import com.ebenjs.models.requests.ResetPasswordRequest;
import com.ebenjs.models.responses.BaseApiResponse;
import com.ebenjs.repositories.UserRepository;
import com.ebenjs.services.mail.MailSendingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    @Value("${app.base.url}")
    private String appBaseUrl;

    @Value("${app.api.prefix}")
    private String apiSuffix;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSendingService mailSendingService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MailSendingService mailSendingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSendingService = mailSendingService;
    }

    public User getUserByEmail(String email) {
        return this.userRepository.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User getUserByActivationHash(String hash) throws Exception {
        return  this.userRepository.getUserByActivationHash(hash)
                .orElseThrow(()-> new Exception("Activation link is broken"));
    }

    public Boolean isPasswordValid(User user, String password){
        return passwordEncoder.matches(password, user.getPassword());
    }

    public User updateUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.userRepository.save(user);
    }

    public BaseApiResponse<Void> forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        Optional<User> user = this.userRepository.getUserByEmail(forgotPasswordRequest.getEmail());
        if (user.isEmpty()){
            log.error("User with email {} not found", forgotPasswordRequest.getEmail());
            return BaseApiResponse.<Void>builder()
                    .status(ApiResponseStatus.ERROR)
                    .httpCode(HttpStatus.NOT_FOUND.value())
                    .message("User not found")
                    .build();
        }

        String passwordResetHash = UUID.randomUUID().toString();
        user.get().setResetPasswordHash(passwordResetHash);
        this.userRepository.save(user.get());

        Context thymeleafContext = new Context();
        String generatedResetLink = generatePasswordResetLink(passwordResetHash, user.get().getEmail());
        thymeleafContext.setVariable("name", user.get().getFirstname());
        thymeleafContext.setVariable("url", generatedResetLink);
        mailSendingService.sendHtmlWithTemplateToOne("user-c7c01a93515e3166@smtp.tickpluswise.com", forgotPasswordRequest.getEmail(), "Testing", thymeleafContext, "emails/password-reset.html");

        return BaseApiResponse.<Void>builder()
                .status(ApiResponseStatus.SUCCESS)
                .httpCode(HttpStatus.OK.value())
                .message("Email sent")
                .build();
    }

    public BaseApiResponse<Void> handlePasswordReset(String hash, String email) {
        Optional<User> user = this.userRepository.getUserByEmailAndResetPasswordHash(email, hash);
        if (user.isEmpty()){
            log.error("This combination of email and hash is not valid for user {}", email);
            return BaseApiResponse.<Void>builder()
                    .status(ApiResponseStatus.ERROR)
                    .httpCode(HttpStatus.NOT_FOUND.value())
                    .message("User not found")
                    .build();
        }
        return BaseApiResponse.<Void>builder()
                .status(ApiResponseStatus.SUCCESS)
                .httpCode(HttpStatus.OK.value())
                .message("Password reset link is valid")
                .build();
    }

    public BaseApiResponse<Void> resetPassword(ResetPasswordRequest resetPasswordRequest) {
        if(this.handlePasswordReset(resetPasswordRequest.getHash(), resetPasswordRequest.getEmail()).getStatus() == ApiResponseStatus.ERROR){
            return BaseApiResponse.<Void>builder()
                    .status(ApiResponseStatus.ERROR)
                    .httpCode(HttpStatus.NOT_FOUND.value())
                    .message("Password reset link is not valid")
                    .build();
        }

        Optional<User> user = this.userRepository.getUserByEmail(resetPasswordRequest.getEmail());
        if (user.isEmpty()){
            log.error("User with email {} not found", resetPasswordRequest.getEmail());
            return BaseApiResponse.<Void>builder()
                    .status(ApiResponseStatus.ERROR)
                    .httpCode(HttpStatus.NOT_FOUND.value())
                    .message("User not found")
                    .build();
        }

        user.get().setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        user.get().setResetPasswordHash(null);
        this.userRepository.save(user.get());
        return BaseApiResponse.<Void>builder()
                .status(ApiResponseStatus.SUCCESS)
                .httpCode(HttpStatus.OK.value())
                .message("Password reset successful")
                .build();
    }

    private String generatePasswordResetLink(String activationHash, String email) {
        return appBaseUrl + apiSuffix + "users/reset-password?hash=" + activationHash + "&email=" + email;
    }

}