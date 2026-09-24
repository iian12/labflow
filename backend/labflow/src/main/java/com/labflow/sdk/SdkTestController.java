package com.labflow.sdk;

import lombok.extern.slf4j.Slf4j;
import com.labflow.project.key.domain.ProjectApiKeyPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/api/v1/sdk/runs")
public class SdkTestController {

    private final AtomicLong runIdGenerator = new AtomicLong(1000);

    /** Returns the verified identity without exposing the API key itself. */
    @GetMapping("/auth-check")
    public ResponseEntity<Map<String, Long>> authCheck(
            @AuthenticationPrincipal ProjectApiKeyPrincipal principal
    ) {
        return ResponseEntity.ok(Map.of(
                "projectId", principal.projectId().value(),
                "apiKeyId", principal.apiKeyId().value(),
                "createdBy", principal.createdBy().value()
        ));
    }

    /*
    * Run 생성 테스트
     */
    @PostMapping
    public ResponseEntity<RunCreateResponse> createRun(
            @RequestBody RunCreateRequest request
            ) {
        long runId = runIdGenerator.incrementAndGet();

        log.info("""
                
                ===== RUN CREATE =====
                runId={}
                name={}
                metadata={}
                ======================
                """,
                runId,
                request.name(),
                request.metadata()
        );

        return ResponseEntity.ok(
                new RunCreateResponse(runId)
        );
    }

    /**
     * Hyperparameter 수신 테스트
     */
    @PostMapping("/{runId}/parameters")
    public ResponseEntity<Void> parameters(
            @PathVariable Long runId,
            @RequestBody ParameterRequest request
    ) {
        log.info("""
                
                ===== PARAMETERS =====
                runId={}
                parameters={}
                ======================
                """,
                runId,
                request.parameters()
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Metric 수신 테스트
     */
    @PostMapping("/{runId}/metrics")
    public ResponseEntity<Void> metrics(
            @PathVariable Long runId,
            @RequestBody MetricRequest request
    ) {
        log.info("""
                
                ===== METRIC =====
                
                runId={}
                epoch={}
                step={}
                elapsedTime={}
                metrics={}
                recordedAt={}
                ==================
                """,

                runId,
                request.epoch(),
                request.step(),
                request.elapsedTime(),
                request.metrics(),
                request.recordedAt()
        );

        return ResponseEntity.noContent().build();
    }


    /**
     * System Metric 수신 테스트
     */
    @PostMapping("/{runId}/system-metrics")
    public ResponseEntity<Void> systemMetrics(
            @PathVariable Long runId,
            @RequestBody SystemMetricRequest request
    ) {
        log.info("""
                
                ===== SYSTEM METRIC =====
             
                runId={}
                elapsedTime={}
                system={}
                recordedAt={}
                =========================
                """,

                runId,
                request.elapsedTime(),
                request.system(),
                request.recordedAt()
        );

        return ResponseEntity.noContent().build();
    }


    /**
     * stdout / stderr 로그 수신 테스트
     */
    @PostMapping("/{runId}/logs")
    public ResponseEntity<Void> logs(
            @PathVariable Long runId,
            @RequestBody LogRequest request
    ) {
        log.info("""
                
                ===== LOG =====
                runId={}
                sequence={}
                stream={}
                message={}
                elapsedTime={}
                recordedAt={}
                ===============
                """,
                runId,
                request.sequence(),
                request.stream(),
                request.message(),
                request.elapsedTime(),
                request.recordedAt()
        );

        return ResponseEntity.noContent().build();
    }


    /**
     * Artifact multipart 업로드 테스트
     */
    @PostMapping(
            value = "/{runId}/artifacts",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> artifact(
            @PathVariable Long runId,

            @RequestPart("file")
            MultipartFile file,

            @RequestParam String type,

            @RequestParam String name,

            @RequestParam(required = false)
            Integer epoch,

            @RequestParam(required = false)
            Long step,

            @RequestParam(defaultValue = "{}")
            String metadata
    ) {
        log.info("""
                
                ===== ARTIFACT =====
                runId={}
                type={}
                name={}
                originalFileName={}
                contentType={}
                size={}
                epoch={}
                step={}
                metadata={}
                ====================
                """,
                runId,
                type,
                name,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                epoch,
                step,
                metadata
        );

        return ResponseEntity.noContent().build();
    }


    /**
     * Run 완료 테스트
     */
    @PostMapping("/{runId}/complete")
    public ResponseEntity<Void> complete(
            @PathVariable Long runId
    ) {
        log.info("""
                
                ===== RUN COMPLETE =====
                runId={}
                ========================
                """,
                runId
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Run 실패 테스트
     */
    @PostMapping("/{runId}/fail")
    public ResponseEntity<Void> fail(
            @PathVariable Long runId,
            @RequestBody RunFailRequest request
    ) {
        log.info("""
                
                ===== RUN FAILED =====
                runId={}
                error={}
                ======================
                """,
                runId,
                request.error()
        );

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // Test Request / Response
    // =========================================================

    public record RunCreateRequest(
            String name,
            Map<String, String> metadata
    ) {
    }

    public record RunCreateResponse(
            Long runId
    ) {
    }

    public record ParameterRequest(
            Map<String, String> parameters
    ) {
    }

    public record MetricRequest(
            Integer epoch,
            Long step,
            Double elapsedTime,
            Map<String, String> metrics,
            Instant recordedAt
    ) {
    }

    public record SystemMetricRequest(
            Double elapsedTime,
            Map<String, Object> system,
            Instant recordedAt
    ) {
    }

    public record LogRequest(
            Long sequence,
            String stream,
            String message,
            Double elapsedTime,
            Instant recordedAt
    ) {
    }

    public record RunFailRequest(
            String error
    ) {
    }
}
