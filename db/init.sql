CREATE TABLE IF NOT EXISTS platforms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(32) UNIQUE NOT NULL
);

INSERT INTO platforms(name)
VALUES ('youtube'), ('rutube'), ('vimeo')
ON CONFLICT (name) DO NOTHING;

CREATE TABLE IF NOT EXISTS urls (
    id BIGSERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    platform_id BIGINT NOT NULL REFERENCES platforms(id),
    telegram_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(url, telegram_user_id)
);

CREATE TABLE IF NOT EXISTS video_stats (
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT NOT NULL REFERENCES urls(id) ON DELETE CASCADE,
    stats BIGINT NOT NULL,
    status BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_video_stats_url_created ON video_stats(url_id, created_at DESC);
