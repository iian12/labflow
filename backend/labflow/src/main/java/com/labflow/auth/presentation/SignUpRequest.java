package com.labflow.auth.presentation;

import com.labflow.auth.application.SignUpCommand;

public record SignUpRequest(

        String email,

        String password,

        String confirmPassword,

        String name) {

    public SignUpCommand toCommand() {
        return new SignUpCommand(
                email().trim(),
                password(),
                confirmPassword(),
                name().trim()
        );
    }
}
