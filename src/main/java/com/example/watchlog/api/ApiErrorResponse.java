package com.example.watchlog.api;

import java.time.OffsetDateTime;

public record ApiErrorResponse(int status, String code, String message, OffsetDateTime timestamp) {}
