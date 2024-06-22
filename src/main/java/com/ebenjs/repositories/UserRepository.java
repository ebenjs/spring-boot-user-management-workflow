package com.ebenjs.repositories;

import com.ebenjs.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByActivationHash(String hash);
    Optional<User> getUserByEmailAndResetPasswordHash(String email, String hash);
}
