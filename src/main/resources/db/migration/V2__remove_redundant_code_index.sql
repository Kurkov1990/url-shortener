-- The UNIQUE constraint on short_urls.code already creates an index automatically.
-- The explicitly created idx_short_urls_code is redundant and wastes storage.
DROP INDEX IF EXISTS idx_short_urls_code;
