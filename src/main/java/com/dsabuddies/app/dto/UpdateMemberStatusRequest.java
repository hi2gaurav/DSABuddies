package com.dsabuddies.app.dto;

public record UpdateMemberStatusRequest(
    String status,
    Integer muteDurationHours,
    String reason
) {}
