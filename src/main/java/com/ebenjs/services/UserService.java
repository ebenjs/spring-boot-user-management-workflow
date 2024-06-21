package com.ebenjs.services;

import com.ebenjs.entities.User;
import com.ebenjs.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByEmail(String email) {
        return this.userRepository.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User getUserByActivationHash(String hash) throws Exception {
        return  this.userRepository.getUserByActivationHash(hash)
                .orElseThrow(()-> new Exception("Activation link is broken"));
    }
}
