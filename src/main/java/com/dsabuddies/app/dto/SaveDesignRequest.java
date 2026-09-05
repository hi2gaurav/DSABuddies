package com.dsabuddies.app.dto;

public record SaveDesignRequest(
    Long templateId,
    String title,
    String content,
    String diagramData
) {}
