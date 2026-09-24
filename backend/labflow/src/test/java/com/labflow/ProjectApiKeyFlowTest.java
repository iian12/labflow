package com.labflow;

import com.labflow.auth.infrastructure.jwt.AccessTokenProvider;
import com.labflow.auth.infrastructure.persistence.JwtAuthenticationFilter;
import com.labflow.global.config.SecurityConfig;
import com.labflow.global.handler.CustomAccessDeniedHandler;
import com.labflow.global.handler.CustomAuthenticationEntryPoint;
import com.labflow.global.id.IdGenerator;
import com.labflow.project.key.application.*;
import com.labflow.project.key.domain.*;
import com.labflow.project.key.domain.repository.ProjectApiKeyRepository;
import com.labflow.project.key.infrastructure.security.ProjectApiKeyAuthenticationFilter;
import com.labflow.project.key.presentation.IssueProjectApiKeyRequest;
import com.labflow.project.key.util.*;
import com.labflow.project.project.*;
import com.labflow.sdk.SdkTestController;
import com.labflow.user.domain.UserId;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tools.jackson.databind.json.JsonMapper;
import java.time.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectApiKeyFlowTest {
    AnnotationConfigWebApplicationContext context;
    MockMvc mvc;
    ProjectApiKeyRepository repository;
    ProjectApiKey key;
    String raw;

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, SdkTestController.class})
    static class Config {
        @Bean ProjectApiKeyRepository repository() { return mock(ProjectApiKeyRepository.class); }
        @Bean ProjectApiKeyHasher hasher() { return new ProjectApiKeyHasher(); }
        @Bean ProjectApiKeyAuthenticationService service(ProjectApiKeyRepository r, ProjectApiKeyHasher h) {
            return new ProjectApiKeyAuthenticationService(r, h);
        }
        @Bean CustomAuthenticationEntryPoint entryPoint() { return new CustomAuthenticationEntryPoint(JsonMapper.builder().build()); }
        @Bean CustomAccessDeniedHandler deniedHandler() { return new CustomAccessDeniedHandler(JsonMapper.builder().build()); }
        @Bean ProjectApiKeyAuthenticationFilter apiFilter(ProjectApiKeyAuthenticationService s, CustomAuthenticationEntryPoint e) {
            return new ProjectApiKeyAuthenticationFilter(s, e);
        }
        @Bean JwtAuthenticationFilter jwtFilter() { return new JwtAuthenticationFilter(mock(AccessTokenProvider.class)); }
    }

    @BeforeEach void setup() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(Config.class);
        context.refresh();
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        repository = context.getBean(ProjectApiKeyRepository.class);
        var projects = mock(ProjectRepository.class);
        var ids = mock(IdGenerator.class);
        when(ids.nextId()).thenReturn(30L);
        var projectId = ProjectId.of(10L);
        var owner = UserId.of(20L);
        var project = mock(Project.class);
        when(project.getCreatedBy()).thenReturn(owner);
        when(projects.findById(projectId)).thenReturn(Optional.of(project));
        when(repository.save(any())).thenAnswer(invocation -> {
            key = invocation.getArgument(0);
            when(repository.findByKeyHash(key.getKeyHash())).thenReturn(Optional.of(key));
            return key;
        });
        var service = new ProjectApiKeyService(projects, new ProjectApiKeyGenerator(), Clock.systemUTC(), ids,
                new ProjectApiKeyHasher(), repository);
        raw = service.issue(owner, projectId, new IssueProjectApiKeyRequest("test")).rawApiKey();
        assertThat(raw).startsWith("lfp_");
        assertThat(key.getKeyHash()).isEqualTo(new ProjectApiKeyHasher().hash(raw)).isNotEqualTo(raw);
        assertThatThrownBy(() -> service.issue(UserId.of(99L), projectId, new IssueProjectApiKeyRequest("other")))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        verify(repository, times(1)).save(any());
    }

    @AfterEach void close() { context.close(); }

    @Test void generatedKeyAuthenticatesAndExposesVerifiedIdentity() throws Exception {
        mvc.perform(get("/api/v1/sdk/runs/auth-check").header("Authorization", "Bearer " + raw))
                .andExpect(status().isOk()).andExpect(jsonPath("$.projectId").value(10))
                .andExpect(jsonPath("$.apiKeyId").value(30));
    }

    @Test void everySdkPostAcceptsKeyWithoutCsrfAndRejectsMissingOrWrongKey() throws Exception {
        String[] suffixes = {"", "/1001/parameters", "/1001/metrics", "/1001/system-metrics", "/1001/logs", "/1001/complete", "/1001/fail"};
        for (String suffix : suffixes) {
            String path = "/api/v1/sdk/runs" + suffix;
            mvc.perform(post(path).header("Authorization", "Bearer " + raw).contentType("application/json").content("{}"))
                    .andExpect(status().is2xxSuccessful());
            mvc.perform(post(path).contentType("application/json").content("{}"))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
            mvc.perform(post(path).header("Authorization", "Bearer wrong").contentType("application/json").content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test void multipartRequiresKey() throws Exception {
        for (String token : new String[]{raw, "wrong", ""}) {
            var request = multipart("/api/v1/sdk/runs/1001/artifacts")
                    .file(new MockMultipartFile("file", "test.txt", "text/plain", new byte[]{1}))
                    .param("type", "model").param("name", "test");
            if (!token.isEmpty()) request.header("Authorization", "Bearer " + token);
            mvc.perform(request).andExpect(status().is(token.equals(raw) ? 204 : 401));
        }
    }

    @Test void revokedAndMalformedKeysAreUnauthorized() throws Exception {
        key.revoke(Instant.now());
        assertThat(key.getStatus()).isEqualTo(ProjectApiKeyStatus.REVOKED);
        for (String token : new String[]{"Bearer " + raw, "Bearer ", "Basic bad"}) {
            mvc.perform(get("/api/v1/sdk/runs/auth-check").header("Authorization", token))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test void preflightDoesNotRequireKeyAndBrowserPostsStillRequireCsrf() throws Exception {
        mvc.perform(options("/api/v1/sdk/runs")).andExpect(status().isOk());
        mvc.perform(post("/api/project-api-keys/issue/10").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }
}
