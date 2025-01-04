-- Database creation used during deployment.

CREATE TABLE IF NOT EXISTS account(

    id            INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(32)  NOT NULL UNIQUE,
    creation_date DATETIME     NOT NULL,
    salt          BINARY(16),  NOT NULL,
    password      BINARY(128), NOT NULL
);

CREATE TABLE IF NOT EXISTS ip_address(

    id                INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ip                VARCHAR(15)  NOT NULL UNIQUE,
    registration_date DATETIME     NOT NULL,
    blacklisted       BOOLEAN      NOT NULL,
);

CREATE TABLE IF NOT EXISTS account_login(

    id              INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id      INT UNSIGNED NOT NULL,
    ip_id           INT UNSIGNED NOT NULL,
    login_date      DATETIME     NOT NULL,
    expiration_date DATETIME     NOT NULL,

    CONSTRAINT fk_logins_user   FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_logins_ip     FOREIGN KEY(ip_id)      REFERENCES ip_address(id)
);

CREATE TABLE IF NOT EXISTS account_session(

    id             INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id     INT UNSIGNED NOT NULL,
    session_cookie BINARY(32)   NOT NULL UNIQUE,

    CONSTRAINT fk_account_session REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS server(

    id        INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ip        VARCHAR(15)  NOT NULL,
    port      INT(5)       NOT NULL,
    name      VARCHAR(100) NOT NULL,
    last_ping DATETIME     NULL, -- TODO Required?

    CONSTRAINT u_server_ip_port UNIQUE(ip, port)
);

-- TODO To discuss
CREATE TABLE IF NOT EXISTS account_joins(

    id           INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id   INT UNSIGNED NOT NULL,
    server_id    INT UNSIGNED NOT NULL,
    join_date    DATETIME     NOT NULL,
    leave_date   DATETIME     NULL,

    CONSTRAINT fk_joins_user   FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_joins_server FOREIGN KEY(server_id)  REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS ban(

    id              INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id      INT UNSIGNED NOT NULL,
    staff_id        INT UNSIGNED NOT NULL,
    where_issued_id INT UNSIGNED NOT NULL, -- TODO Needed?
    reason          VARCHAR(256) NOT NULL, -- TODO Check for memory reason if 255 is better.
    when_issued     DATETIME     NOT NULL,
    expiration_date DATETIME,    NULL

    CONSTRAINT fk_ban_user         FOREIGN KEY(account_id)      REFERENCES account(id),
    CONSTRAINT fk_ban_staff        FOREIGN KEY(staff_id)        REFERENCES account(id),
    CONSTRAINT fk_ban_where_issued FOREIGN KEY(where_issued_id) REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS unban(

    id              INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ban_id          INT UNSIGNED NOT NULL UNIQUE,
    staff_id        INT UNSIGNED NOT NULL,
    where_issued_id INT UNSIGNED NOT NULL, -- TODO Needed?
    when_issued     DATETIME     NOT NULL,

    CONSTRAINT fk_unbans_ban          FOREIGN KEY(ban_id)          REFERENCES ban(id),
    CONSTRAINT fk_unbans_staff        FOREIGN KEY(staff_id)        REFERENCES account(id),
    CONSTRAINT fk_unbans_where_issued FOREIGN KEY(where_issued_id) REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS kick(

    id              INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id      INT UNSIGNED NOT NULL,
    staff_id        INT UNSIGNED NOT NULL,
    server_id       INT UNSIGNED NOT NULL,
    where_issued_id INT UNSIGNED NOT NULL, -- TODO Needed?
    reason          VARCHAR(256) NOT NULL,
    when_issued     DATETIME     NOT NULL,

    CONSTRAINT fk_kicks_user         FOREIGN KEY(account_id)      REFERENCES account(id),
    CONSTRAINT fk_kicks_staff        FOREIGN KEY(staff_id)        REFERENCES account(id),
    CONSTRAINT fk_kicks_server       FOREIGN KEY(server_id)       REFERENCES server(id),
    CONSTRAINT fk_kicks_where_issued FOREIGN KEY(where_issued_id) REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS warn(

    id              INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id      INT UNSIGNED NOT NULL,
    staff_id        INT UNSIGNED NOT NULL,
    where_issued_id INT UNSIGNED NOT NULL, -- TODO Needed?
    reason          VARCHAR(256) NOT NULL,
    when_issued     DATETIME     NOT NULL,
    received        BOOLEAN      NOT NULL,

    CONSTRAINT fk_warns_user         FOREIGN KEY(account_id)      REFERENCES account(id),
    CONSTRAINT fk_warns_staff        FOREIGN KEY(staff_id)        REFERENCES account(id),
    CONSTRAINT fk_warns_where_issued FOREIGN KEY(where_issued_id) REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS role(

    id    INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(256) NOT NULL,
    color CHAR(8)      NOT NULL,
    level INT(3)       NOT NULL
);

CREATE TABLE IF NOT EXISTS account_role(

    id         INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id INT UNSIGNED NOT NULL,
    role_id    INT UNSIGNED NOT NULL,

    CONSTRAINT fk_roles_user FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_roles_role FOREIGN KEY(role_id)    REFERENCES role(id)
);

-- TODO Integrate inside of the role table instead?
CREATE TABLE IF NOT EXISTS symbol(

    id     INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    symbol CHAR(1)      NOT NULL,

    CONSTRAINT u_symbols_symbol UNIQUE(symbol)
);

CREATE TABLE IF NOT EXISTS permission(

    id       INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    property VARCHAR(200) NOT NULL UNIQUE,
    name     VARCHAR(100) NOT NULL, -- TODO Remove and only use the property?
);

CREATE TABLE IF NOT EXISTS role_permission(

    id            INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_id       INT UNSIGNED NOT NULL,
    permission_id INT UNSIGNED NOT NULL,

    CONSTRAINT fk_role       FOREIGN KEY(role_id)       REFERENCES role(id),
    CONSTRAINT fk_permission FOREIGN KEY(permission_id) REFERENCES permission(id)
);
