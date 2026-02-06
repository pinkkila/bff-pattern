CREATE TABLE IF NOT EXISTS message
(
    id           SERIAL PRIMARY KEY,
    content      TEXT NOT NULL,
    user_id      TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_message_user_id ON message (user_id);