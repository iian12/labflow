package com.labflow.user.infrastructure.persistence;

import com.labflow.global.id.AssignedIdEntity;
import com.labflow.user.domain.AccountStatus;
import com.labflow.user.domain.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "users")
public class UserEntity extends AssignedIdEntity {

    private String email;

    private String encodedPassword;

    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @Builder
    public UserEntity(Long id, String email, String encodedPassword, String name, Role role, AccountStatus accountStatus) {
        this.id = id;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.name = name;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    public void updateAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }
}
