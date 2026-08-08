package com.apexledger.shared.web;

public record ErrorResponse(String code, String message, String traceId) {
}
