package com.labflow.user.domain.repository;

import com.labflow.user.domain.UserId;
import com.labflow.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    void update(User user);

    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
