package com.ebenjs.controllers;

import com.ebenjs.entities.User;
import com.ebenjs.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
