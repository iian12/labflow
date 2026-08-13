package com.labflow.user.application;

import com.labflow.user.domain.AccountStatus;
import com.labflow.user.domain.User;
import com.labflow.user.domain.UserId;
import com.labflow.user.domain.repository.UserRepository;
import com.labflow.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void activateAccountStatus(UserId userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.activate();
        userRepository.update(user);
    }
}
