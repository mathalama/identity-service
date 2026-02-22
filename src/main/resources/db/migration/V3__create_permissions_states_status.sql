ALTER TABLE users
    ADD account_state SMALLINT;

ALTER TABLE users
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE users
    ADD permissions VARCHAR(255);

ALTER TABLE users
    ADD security_status SMALLINT;

ALTER TABLE roles
DROP
COLUMN id;

ALTER TABLE roles
    ADD id UUID NOT NULL PRIMARY KEY;

ALTER TABLE users
DROP
COLUMN id;

ALTER TABLE users_roles
DROP
COLUMN role_id;

ALTER TABLE users
    ADD id UUID NOT NULL PRIMARY KEY;

ALTER TABLE users_roles
DROP
COLUMN user_id;

ALTER TABLE users_roles
    ADD role_id UUID NOT NULL PRIMARY KEY;

ALTER TABLE users_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE users_roles
    ADD user_id UUID NOT NULL PRIMARY KEY;

ALTER TABLE users_roles
    ADD CONSTRAINT fk_userol_on_users FOREIGN KEY (user_id) REFERENCES users (id);