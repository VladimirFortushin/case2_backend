package ru.mephi.case2.service;

import ru.mephi.case2.db.VideoRepository;
import ru.mephi.case2.db.entity.Platform;
import ru.mephi.case2.db.entity.UrlInfo;
import ru.mephi.case2.db.entity.UrlStatsRow;
import ru.mephi.case2.db.entity.UserSummary;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

public class LinkService {

    private static final Pattern YOUTUBE_PATTERN = Pattern.compile("^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/).+");
    private static final Pattern RUTUBE_PATTERN = Pattern.compile("^(https?://)?(www\\.)?rutube\\.ru/video/.+");

    private final VideoRepository repository;

    public LinkService(VideoRepository repository) {
        this.repository = repository;
    }

    public boolean addLink(long telegramUserId, String url) {
        validateUrl(url);
        Platform platform = detectPlatform(url);
        if (platform == Platform.UNKNOWN) {
            throw new IllegalArgumentException("Поддерживаются только YouTube и RuTube ссылки.");
        }
        return repository.addUrl(url, platform, telegramUserId);
    }

    public List<UrlStatsRow> list(long telegramUserId) {
        return repository.getUrlStatsForUser(telegramUserId);
    }

    public UserSummary summary(long telegramUserId) {
        return repository.getSummaryForUser(telegramUserId);
    }

    public List<UrlInfo> allUrls() {
        return repository.getUrlsWithPlatforms();
    }

    private void validateUrl(String url) {
        try {
            URI uri = URI.create(url.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("Невалидный URL.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Невалидный URL.");
        }
    }

    private Platform detectPlatform(String url) {
        if (YOUTUBE_PATTERN.matcher(url).matches()) {
            return Platform.YOUTUBE;
        }
        if (RUTUBE_PATTERN.matcher(url).matches()) {
            return Platform.RUTUBE;
        }
        return Platform.UNKNOWN;
    }
}
