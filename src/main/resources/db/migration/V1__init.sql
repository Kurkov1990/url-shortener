CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE short_urls (
                            id BIGSERIAL PRIMARY KEY,
                            version BIGINT NOT NULL DEFAULT 0,
                            code VARCHAR(8) NOT NULL UNIQUE,
                            original_url VARCHAR(2048) NOT NULL,
                            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                            click_count BIGINT NOT NULL DEFAULT 0,
                            active BOOLEAN NOT NULL DEFAULT TRUE,
                            owner_id BIGINT NOT NULL,
                            CONSTRAINT fk_short_urls_owner
                                FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_short_urls_code ON short_urls(code);
CREATE INDEX idx_short_urls_owner_id ON short_urls(owner_id);