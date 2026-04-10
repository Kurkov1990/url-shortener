ALTER TABLE short_urls ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;
UPDATE short_urls SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE short_urls ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE short_urls ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;
