package com.labflow.auth.domain;

import com.labflow.user.domain.Role;
import com.labflow.user.domain.UserId;

public record AccessTokenClaims(UserId userId, Role role) {
}
