CREATE TABLE urls (
    id          BIGSERIAL PRIMARY KEY,
    short_code  VARCHAR(10)  UNIQUE NOT NULL,
    stats_token VARCHAR(20)  UNIQUE NOT NULL,
    long_url    TEXT NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW(),
    expires_at  TIMESTAMP,
    user_ip     VARCHAR(45)
);

CREATE TABLE clicks (
    id          BIGSERIAL PRIMARY KEY,
    url_id      BIGINT REFERENCES urls(id) ON DELETE CASCADE,
    clicked_at  TIMESTAMP DEFAULT NOW(),
    country     VARCHAR(50),
    city        VARCHAR(50),
    referrer    TEXT,
    user_agent  TEXT
);

CREATE INDEX idx_urls_short_code  ON urls(short_code);
CREATE INDEX idx_urls_stats_token ON urls(stats_token);
CREATE INDEX idx_urls_expires_at  ON urls(expires_at);
CREATE INDEX idx_clicks_url_id    ON clicks(url_id);
