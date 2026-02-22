CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    permissions VARCHAR(255),
    account_state VARCHAR(50),
    security_status VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);
