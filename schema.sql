-- 1. Таблица платформ
CREATE TABLE IF NOT EXISTS platforms (
    id SERIAL PRIMARY KEY,              
    name VARCHAR(10) NOT NULL UNIQUE
);

-- 2. Таблица URL
CREATE TABLE IF NOT EXISTS urls (
    id SERIAL PRIMARY KEY,
    url VARCHAR(2048) NOT NULL UNIQUE,
    platform_id INTEGER NOT NULL,
    
    -- Внешние ключи
    FOREIGN KEY (platform_id) REFERENCES platforms(id) ON DELETE CASCADE
);

-- 3. Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY, 
    telegram_id INTEGER NOT NULL UNIQUE
);

-- 4. Связи пользователей и URL
CREATE TABLE IF NOT EXISTS users_urls (
    id SERIAL PRIMARY KEY, 
    user_id INTEGER NOT NULL,
    url_id INTEGER NOT NULL,
    
    -- Внешние ключи
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (url_id) REFERENCES urls(id) ON DELETE CASCADE
);

-- 5. Статистика видео
CREATE TABLE IF NOT EXISTS video_stats (
    id SERIAL PRIMARY KEY, 
    url_id INTEGER NOT NULL,  
    stats INTEGER DEFAULT 0,
    status BOOLEAN DEFAULT TRUE, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (url_id) REFERENCES urls(id) ON DELETE CASCADE
);

INSERT INTO platforms (name) VALUES ('youtube') ON CONFLICT (name) DO NOTHING;
INSERT INTO platforms (name) VALUES ('rutube') ON CONFLICT (name) DO NOTHING;
INSERT INTO platforms (name) VALUES ('vimeo') ON CONFLICT (name) DO NOTHING;

