-- Database creation used during deployment.

CREATE TABLE IF NOT EXISTS account(

    id               INT(11)             NOT NULL PRIMARY KEY AUTO_INCREMENT,
    username         VARCHAR(32)         NOT NULL,
    account_creation BIGINT(20) UNSIGNED NOT NULL,
    salt             BINARY(16),         NOT NULL,
    password         BINARY(128),        NOT NULL
    
    CONSTRAINT u_account_username UNIQUE(username)
);

CREATE TABLE IF NOT EXISTS ip_address(

    id                INT(11)             NOT NULL PRIMARY KEY AUTO_INCREMENT,
    ip                VARCHAR(15)         NOT NULL,
    registration_date BIGINT(20) UNSIGNED NOT NULL,
    blacklisted       BOOLEAN             NOT NULL,
    
    CONSTRAINT u_ip_address_ip UNIQUE(ip)
);

CREATE TABLE IF NOT EXISTS account_login(

    id              INT(11)             NOT NULL PRIMARY KEY AUTO_INCREMENT ,
    account_id      INT(11)             NOT NULL,
    ip_id           INT(11)             NOT NULL,
    session         BINARY(32),         NULL -- TODO Cannot be null, move to a session table?
    login_date      BIGINT(20) unsigned NOT NULL,
    expiration_date BIGINT(20) unsigned NOT NULL,

    CONSTRAINT u_logins_session UNIQUE(session),
    CONSTRAINT fk_logins_user   FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_logins_ip     FOREIGN KEY(ip_id)      REFERENCES ip_address(id)
);

CREATE TABLE IF NOT EXISTS server(

    id        INT(11)      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    ip        VARCHAR(15)  NOT NULL,
    port      INT(5)       NOT NULL,
    name      VARCHAR(100) NOT NULL,
    last_ping BIGINT(20) unsigned DEFAULT 0,

    CONSTRAINT u_servers_ip_port UNIQUE(ip, port)
);

-- TODO To discuss
CREATE TABLE IF NOT EXISTS user_joins(

    id           INT(11)             NOT NULL PRIMARY KEY AUTO_INCREMENT,
    account_id   INT(11)             NOT NULL,
    server_id    INT(11)             NOT NULL,
    join_date    BIGINT(20) unsigned NOT NULL,
    leave_date   BIGINT(20) unsigned NOT NULL DEFAULT 0,

    CONSTRAINT fk_joins_user   FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_joins_server FOREIGN KEY(server_id)  REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS ban(

    id              INT(11)      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    account_id      INT(11)      NOT NULL,
    staff_id        INT(11)      NOT NULL,
    where_issued_id INT(11)      NOT NULL,
    reason          VARCHAR(256) NOT NULL,
    when_issued     BIGINT(20)   NOT NULL,
    expiration_date BIGINT(20), -- TODO Not sure about this one

    CONSTRAINT fk_bans_user         FOREIGN KEY(account_id)      REFERENCES account(id),
    CONSTRAINT fk_bans_staff        FOREIGN KEY(staff_id)        REFERENCES account(id),
    CONSTRAINT fk_bans_where_issued FOREIGN KEY(where_issued_id) REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS unban(

    id              INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    ban_id          INT(11) NOT NULL,
    staff_id        INT(11) NOT NULL,
    where_issued_id INT(11) NOT NULL,
    when_issued     BIGINT(20) NOT NULL,

    CONSTRAINT u_unbans_ban           UNIQUE(ban_id),
    CONSTRAINT fk_unbans_ban          FOREIGN KEY(ban_id)          REFERENCES ban(id),
    CONSTRAINT fk_unbans_staff        FOREIGN KEY(staff_id)        REFERENCES account(id),
    CONSTRAINT fk_unbans_where_issued FOREIGN KEY(where_issued_id) REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS kick(

    id              INT(11)      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    account_id      INT(11)      NOT NULL,
    staff_id        INT(11)      NOT NULL,
    server_id       INT(11)      NOT NULL,
    where_issued_id INT(11)      NOT NULL,
    reason          VARCHAR(256) NOT NULL,
    when_issued     BIGINT(20)   NOT NULL,

    CONSTRAINT fk_kicks_user         FOREIGN KEY(account_id)      REFERENCES account(id),
    CONSTRAINT fk_kicks_staff        FOREIGN KEY(staff_id)        REFERENCES account(id),
    CONSTRAINT fk_kicks_server       FOREIGN KEY(server_id)       REFERENCES server(id),
    CONSTRAINT fk_kicks_where_issued FOREIGN KEY(where_issued_id) REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS warn(

    id              INT(11)      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    account_id      INT(11)      NOT NULL,
    staff_id        INT(11)      NOT NULL,
    where_issued_id INT(11)      NOT NULL,
    reason          VARCHAR(256) NOT NULL,
    when_issued     BIGINT(20)   NOT NULL,
    received        BOOLEAN      NOT NULL,

    CONSTRAINT fk_warns_user         FOREIGN KEY(account_id)      REFERENCES account(id),
    CONSTRAINT fk_warns_staff        FOREIGN KEY(staff_id)        REFERENCES account(id),
    CONSTRAINT fk_warns_where_issued FOREIGN KEY(where_issued_id) REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS role(

    id    INT(11)      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name  VARCHAR(256) NOT NULL,
    color char(8)      NOT NULL,
    level INT(3)       NOT NULL
);

CREATE TABLE IF NOT EXISTS users_roles(

    id         INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    account_id INT(11) NOT NULL,
    role_id    INT(11) NOT NULL,

    CONSTRAINT fk_roles_user FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_roles_role FOREIGN KEY(role_id)    REFERENCES role(id)
);

-- TODO Integrate inside of the role table instead?
CREATE TABLE IF NOT EXISTS symbols(

    id     INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    symbol char(1) NOT NULL,

    CONSTRAINT u_symbols_symbol UNIQUE(symbol)
);

CREATE TABLE IF NOT EXISTS permission(

    id       INT(11)      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    property VARCHAR(200) NOT NULL,
    name     VARCHAR(100) NOT NULL, -- TODO Remove and only use the property?

    CONSTRAINT u_permissions_property UNIQUE(property)
);

CREATE TABLE IF NOT EXISTS role_permission(

    id            INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    role_id       INT(11) NOT NULL,
    permission_id INT(11) NOT NULL,

    CONSTRAINT fk_role       FOREIGN KEY(role_id)       REFERENCES role(id),
    CONSTRAINT fk_permission FOREIGN KEY(permission_id) REFERENCES permission(id)
);
