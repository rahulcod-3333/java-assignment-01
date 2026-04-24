package com.internship.backend_assignment.service;

public interface InteractionService {

    void incrementViralityScore(Long postId, String interactionType);

    void validateBotInteraction(Long postId, Long botId, Long humanAuthorId);

    int pointsForInteraction(String type);
}
