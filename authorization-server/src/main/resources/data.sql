TRUNCATE TABLE app_user RESTART IDENTITY CASCADE;

INSERT INTO app_user(username, password)
VALUES ('user', '{noop}password');