package com.labflow.sdk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/sdk/test")
public class SdkTestController {

    @PostMapping
    public ResponseEntity<SdkTestResponse> test(@RequestHeader("Authorization") String authorization,
                                                @RequestBody SdkTestRequest request) {
        if (!authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is invalid");
        }

        String apiKey = authorization.substring(7);

        String maskedKey = apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : "***";
        log.info(maskedKey);
        return ResponseEntity.ok(new SdkTestResponse(true, maskedKey, request.message()));
    }
}
