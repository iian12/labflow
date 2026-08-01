package com.labflow.user.infrastructure.persistence;

import com.labflow.user.domain.UserId;
import com.labflow.user.domain.AccountStatus;
import com.labflow.user.domain.Users;

public class UserMapper {
    private UserMapper() {
        /* This utility class should not be instantiated */
    }


    // 도메인 -> 엔티티 변환용 팩토리 메서드
    public static UserEntity toEntity(Users domain) {
        return UserEntity.builder()
                .id(domain.getId().value())
                .encodedPassword(domain.getEncodedPassword())
                .name(domain.getName())
                .email(domain.getEmail())
                .role(domain.getRole())
                .accountStatus(domain.getAccountStatus())
                .build();
    }

    // 엔티티 -> 도메인 변환용 팩토리 메서드
    public static Users toDomain(UserEntity entity) {
        return Users.restore(
                UserId.of(entity.getId()),
                entity.getEmail(),
                entity.getEncodedPassword(),
                entity.getName(),
                entity.getRole(),
                entity.getAccountStatus()
        );
    }

    public static void updateEntity(UserEntity entity, Users users) {
        entity.updateAccountStatus(AccountStatus.ACTIVE);
    }
}
