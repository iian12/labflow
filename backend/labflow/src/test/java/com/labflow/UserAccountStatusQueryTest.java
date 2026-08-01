package com.labflow;

import com.labflow.user.application.UserService;
import com.labflow.user.domain.AccountStatus;
import com.labflow.user.domain.Role;
import com.labflow.user.infrastructure.persistence.UserEntity;
import com.labflow.user.domain.UserId;
import com.labflow.user.infrastructure.persistence.UserJpaRepository;
import com.labflow.user.infrastructure.persistence.UserRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({
        UserRepositoryImpl.class,
        UserService.class
})
class UserAccountStatusQueryTest {

    private final UserJpaRepository userJpaRepository;
    private final UserService userService;
    private final EntityManager entityManager;
    private final Statistics statistics;

    @Autowired
    UserAccountStatusQueryTest(
            UserJpaRepository userJpaRepository,
            UserService userService,
            EntityManager entityManager,
            EntityManagerFactory entityManagerFactory
    ) {
        this.userJpaRepository = userJpaRepository;
        this.userService = userService;
        this.entityManager = entityManager;

        this.statistics = entityManagerFactory
                .unwrap(SessionFactory.class)
                .getStatistics();
    }


    @BeforeEach
    void setUp() {
        statistics.setStatisticsEnabled(true);
    }

    @Test
    void accountStatus를_변경할_때_SELECT는_한_번만_실행된다() {
        UserEntity entity = UserEntity.builder()
                .id(55111111411052456L)
                .email("query-test@example.com")
                .encodedPassword("password")
                .name("Minchan")
                .role(Role.USER)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .build();

        userJpaRepository.saveAndFlush(entity);

        entityManager.clear();

        statistics.clear();

        UserId userId = UserId.of(entity.getId());

        userService.updateAccountStatus(
                userId,
                AccountStatus.ACTIVE
        );

        long entityLoadCount =
                statistics.getEntityLoadCount();

        long entityUpdateCount =
                statistics.getEntityUpdateCount();

        long preparedStatementCount =
                statistics.getPrepareStatementCount();

        System.out.println("entityLoadCount = " + entityLoadCount);
        System.out.println("entityUpdateCount = " + entityUpdateCount);
        System.out.println("preparedStatementCount = " + preparedStatementCount);

        assertThat(statistics.getEntityLoadCount()).isEqualTo(1);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    }
}
