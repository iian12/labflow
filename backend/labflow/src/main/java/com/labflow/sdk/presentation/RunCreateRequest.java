package com.labflow.sdk.presentation;

import java.util.Map;

public record RunCreateRequest(String name, Map<String, String> metadata) {
}
