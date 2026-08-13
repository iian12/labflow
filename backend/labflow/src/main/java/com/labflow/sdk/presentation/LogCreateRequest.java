package com.labflow.sdk.presentation;

import java.time.Instant;

public record LogCreateRequest(Long sequence, String stream, String message, Double elapsedTime, Instant recordedAt) {
}
