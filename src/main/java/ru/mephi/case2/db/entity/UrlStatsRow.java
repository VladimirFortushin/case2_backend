package ru.mephi.case2.db.entity;

public record UrlStatsRow(String url, Platform platform, Long lastSuccessfulViews, boolean latestStatus, String latestStatusText) {
}
