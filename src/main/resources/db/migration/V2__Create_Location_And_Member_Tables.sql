CREATE TABLE locations
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255)             NOT NULL,
    description TEXT,
    is_active   BOOLEAN                  NOT NULL DEFAULT true,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE members
(
    id           UUID PRIMARY KEY,
    user_id      UUID UNIQUE,
    first_name   VARCHAR(100)             NOT NULL,
    last_name    VARCHAR(100)             NOT NULL,
    phone_number VARCHAR(20),
    employee_id  VARCHAR(50) UNIQUE,
    is_active    BOOLEAN                  NOT NULL DEFAULT true,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);