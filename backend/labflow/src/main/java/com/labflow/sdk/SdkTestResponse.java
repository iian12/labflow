package com.labflow.sdk;

public record SdkTestResponse(boolean authenticated,
                             String apiKeyPrefix,
                             String receivedMessage) {
}
