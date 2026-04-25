package ru.mephi.case2.db.entity;

import java.time.LocalDateTime;

public record VideoStats(long urlId, long stats, boolean status, LocalDateTime createdAt) {
}
