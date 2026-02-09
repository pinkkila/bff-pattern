CREATE TABLE IF NOT EXISTS app_user
(
    id       uuid DEFAULT uuidv7() PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);