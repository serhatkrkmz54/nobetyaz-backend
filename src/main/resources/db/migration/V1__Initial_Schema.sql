CREATE TABLE system_settings
(
    setting_key   VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(255)             NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    username   VARCHAR(50) UNIQUE       NOT NULL,
    email      VARCHAR(255) UNIQUE      NOT NULL,
    password   VARCHAR(255)             NOT NULL,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    is_active  BOOLEAN                  NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE user_roles
(
    user_id UUID    NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE CASCADE
);