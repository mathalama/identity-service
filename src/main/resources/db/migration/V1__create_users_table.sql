CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    permissions VARCHAR(255) NOT NULL
    account_state VARCHAR(255) NOT NULL,
    security_status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
