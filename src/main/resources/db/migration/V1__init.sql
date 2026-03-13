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
```

---

**What changed from before:**

Only one addition — `stats_token` column in the `urls` table:

| Column | What it stores | Example |
|---|---|---|
| `stats_token` | Random unique token generated when URL is created | `x9Kp2mQr4nZw` |

And a new index `idx_urls_stats_token` because every stats page request will look up by this token — needs to be fast.

---

**How a full row looks now:**
```
-- id          → 1
-- short_code  → kR9mXz
-- stats_token → x9Kp2mQr4nZw
-- long_url    → https://www.youtube.com/watch?v=abc123
-- created_at  → 2025-03-13 10:30:00
-- expires_at  → null
-- user_ip     → 103.21.58.12