package com.labflow.user.domain.repository;

import com.labflow.user.domain.UserId;
import com.labflow.user.domain.Users;

import java.util.Optional;

public interface UserRepository {

    Users save(Users user);

    void update(Users user);

    Optional<Users> findById(UserId userId);

    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);
}
