package com.ebenjs.controllers;

import com.ebenjs.entities.User;
import com.ebenjs.enums.ApiResponseStatus;
import com.ebenjs.models.requests.ChangePasswordRequest;
import com.ebenjs.models.requests.ForgotPasswordRequest;
import com.ebenjs.models.requests.ResetPasswordRequest;
import com.ebenjs.models.responses.BaseApiResponse;
import com.ebenjs.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public User getUserByEmail(@RequestParam String email){
        return this.userService.getUserByEmail(email);
    }

    @Operation(
            summary = "Update user password",
            description = "Update user password by providing the user email and the new password." +
                    "This endpoint is used to update the password of the user. The user must be authenticated.")
    @PutMapping("/update-password")
    public BaseApiResponse<Void> updateUserPassword(@RequestBody ChangePasswordRequest changePasswordRequest, @AuthenticationPrincipal User user){
        Boolean isPasswordValid = this.userService.isPasswordValid(user, changePasswordRequest.getPassword());
        if (Boolean.FALSE.equals(isPasswordValid)){
            throw new RuntimeException("Password is not valid");
        }
        User editedUser = this.userService.updateUserPassword(user, changePasswordRequest.getNewPassword());
        if (editedUser == null){
            throw new RuntimeException("Password update failed");
        }

        return BaseApiResponse.<Void>builder()
                .status(ApiResponseStatus.SUCCESS)
                .httpCode(HttpStatus.OK.value())
                .message("Password updated successfully")
                .build();
    }

    @Operation(
            summary = "Forgot password",
            description = "Send email to get a link to reset password.")
    @PostMapping("/forgot-password")
    public BaseApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest){
        return this.userService.forgotPassword(forgotPasswordRequest);
    }

    @Operation(
            summary = "Handle password reset",
            description = "Handle password reset by providing the hash and the email." +
                    "This endpoint is used to verify the hash and the email. It does not reset the password.")
    @GetMapping("/handle-password-reset")
    public BaseApiResponse<Void> handlePasswordReset(@RequestParam String hash, @RequestParam String email){
        return this.userService.handlePasswordReset(hash, email);
    }

    @Operation(
            summary = "Reset password",
            description = "Reset password by providing the email and the new password")
    @PutMapping("/reset-password")
    public BaseApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest){
        return this.userService.resetPassword(resetPasswordRequest);
    }
}
