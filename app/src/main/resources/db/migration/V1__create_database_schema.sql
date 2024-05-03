CREATE TABLE users
(
    user_pk           BIGSERIAL PRIMARY KEY,
    username          VARCHAR(50)              NOT NULL UNIQUE,
    email             VARCHAR(256)             NOT NULL UNIQUE,
    password_hash     VARCHAR(256)             NOT NULL,
    registration_date TIMESTAMP WITH TIME ZONE NOT NULL,
    role              SMALLINT                 NOT NULL
);

CREATE TABLE refresh_tokens
(
    token_id   BIGSERIAL PRIMARY KEY,
    user_fk    BIGINT                   NOT NULL,
    token      VARCHAR(1024)            NOT NULL,
    issued_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status     SMALLINT                 NOT NULL,
    FOREIGN KEY (user_fk) REFERENCES users (user_pk)
);
