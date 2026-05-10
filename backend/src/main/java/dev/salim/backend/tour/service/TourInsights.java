package dev.salim.backend.tour.service;

public record TourInsights(
    int popularity,
    String popularityLabel,
    int childFriendlinessScore,
    String childFriendliness,
    String achievementBadge
) {}
