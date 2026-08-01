package com.labflow.user.domain;

import lombok.Getter;

@Getter
public class Users {

    private final UserId id;
    private final String email;
    private String encodedPassword;
    private String name;
    private Role role;
    private AccountStatus accountStatus;

    public Users(UserId id, String email, String encodedPassword, String name, Role role, AccountStatus accountStatus) {
        this.id = id;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.name = name;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    public static Users createPendingUser(UserId id, String email, String encodedPassword, String name) {
        return new Users(id, email, encodedPassword, name, Role.USER, AccountStatus.PENDING_VERIFICATION);
    }

    public static Users restore(UserId id, String email, String encodedPassword, String name, Role role, AccountStatus accountStatus) {
        return new Users(id, email, encodedPassword, name, role, accountStatus);
    }

    public void update(String encodedPassword, String name, Role role) {
        this.encodedPassword = encodedPassword;
        this.name = name;
        this.role = role;
    }

    public void activate() {
        if (accountStatus != AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Account is already active");
        }

        accountStatus = AccountStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

}
