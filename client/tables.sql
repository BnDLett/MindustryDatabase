-- Database creation used during deployment.

CREATE TABLE IF NOT EXISTS account(

    id            INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(32)  NOT NULL UNIQUE,
    display_name  TINYTEXT     NOT NULL,
    creation_date DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3),
    salt          BINARY(16)   NOT NULL,
    password      BINARY(128)  NOT NULL,

    -- I use the discord username validation since they fit our use cases.
    CONSTRAINT chk_username_valid CHECK (
            CHAR_LENGTH(username) BETWEEN 2 AND 32 AND
            username REGEXP '^[a-z0-9_.]+$' AND
            username NOT REGEXP '\\.\\.'
    )
);

CREATE TABLE IF NOT EXISTS server(

    id               INT UNSIGNED      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ip_address       INET4             NOT NULL,
    port             SMALLINT UNSIGNED NOT NULL,
    name             TINYTEXT          NOT NULL,
    -- Last heartbeat available used in case the server goes offline.
    heartbeat        DATETIME(3)       NOT NULL DEFAULT UTC_TIMESTAMP(3),
    -- The period of the heartbeat in milliseconds.
    heartbeat_period INT UNSIGNED      NOT NULL DEFAULT 5000,

    CONSTRAINT u_server_ip_port UNIQUE(ip_address, port)
);

CREATE TABLE IF NOT EXISTS account_login(

    id         INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id INT UNSIGNED NOT NULL,
    ip_address INET4        NOT NULL,
    login_date DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3),

    CONSTRAINT fk_account_login_user FOREIGN KEY(account_id) REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS account_session(

    id              INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id      INT UNSIGNED NOT NULL,
    session_cookie  BINARY(32)   NOT NULL UNIQUE,
    expiration_date DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3),

    CONSTRAINT fk_account_session FOREIGN KEY(account_id) REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS account_server_join(

    id         INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id INT UNSIGNED NOT NULL,
    server_id  INT UNSIGNED NOT NULL,
    join_date  DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3),
    leave_date DATETIME(3)  NULL     DEFAULT NULL,

    CONSTRAINT fk_account_server_join_user   FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_account_server_join_server FOREIGN KEY(server_id)  REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS account_report(

    id            INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id    INT UNSIGNED NOT NULL,
    reported_id   INT UNSIGNED NOT NULL,
    short_reason  TINYTEXT     NOT NULL,
    long_reason   TEXT         NOT NULL DEFAULT '',
    creation_date DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3),

    CONSTRAINT fk_report_account  FOREIGN KEY(account_id)  REFERENCES account(id),
    CONSTRAINT fk_report_reported FOREIGN KEY(reported_id) REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS account_report_reply(

    id            INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    report_id     INT UNSIGNED NOT NULL UNIQUE,
    staff_id      INT UNSIGNED NOT NULL,
    accepted      BOOLEAN      NOT NULL,
    message       TEXT         NOT NULL DEFAULT '',
    creation_date DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3),

    CONSTRAINT fk_report_reply_staff  FOREIGN KEY(staff_id)  REFERENCES account(id),
    CONSTRAINT fk_report_reply_report FOREIGN KEY(report_id) REFERENCES report(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ban(

    id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id  INT UNSIGNED NOT NULL,
    staff_id    INT UNSIGNED NOT NULL,
    server_id   INT UNSIGNED NOT NULL,
    reason      TEXT         NOT NULL,
    when_issued DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3),
    -- Null for bans that are permanent.
    expiration_date DATETIME(3) NULL,

    CONSTRAINT fk_ban_user   FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_ban_staff  FOREIGN KEY(staff_id)   REFERENCES account(id),
    CONSTRAINT fk_ban_server FOREIGN KEY(server_id)  REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS unban(

    id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ban_id      INT UNSIGNED NOT NULL UNIQUE,
    staff_id    INT UNSIGNED NOT NULL,
    when_issued DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3),

    CONSTRAINT fk_unban_ban   FOREIGN KEY(ban_id)   REFERENCES ban(id),
    CONSTRAINT fk_unban_staff FOREIGN KEY(staff_id) REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS kick(

    id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id  INT UNSIGNED NOT NULL,
    staff_id    INT UNSIGNED NOT NULL,
    server_id   INT UNSIGNED NOT NULL,
    reason      TEXT         NOT NULL,
    when_issued DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3),

    CONSTRAINT fk_kick_user   FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_kick_staff  FOREIGN KEY(staff_id)   REFERENCES account(id),
    CONSTRAINT fk_kick_server FOREIGN KEY(server_id)  REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS warn(

    id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id  INT UNSIGNED NOT NULL,
    staff_id    INT UNSIGNED NOT NULL,
    server_id   INT UNSIGNED NOT NULL,
    reason      TEXT         NOT NULL,
    when_issued DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3),
    -- False if the warn needs to be shown to the user, true if the user has read it.
    received    BOOLEAN      NOT NULL,

    CONSTRAINT fk_warn_user   FOREIGN KEY(account_id) REFERENCES account(id),
    CONSTRAINT fk_warn_staff  FOREIGN KEY(staff_id)   REFERENCES account(id),
    CONSTRAINT fk_warn_server FOREIGN KEY(server_id)  REFERENCES server(id)
);

CREATE TABLE IF NOT EXISTS ip_blacklist(

    id            INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    staff_id      INT UNSIGNED NOT NULL,
    ip_address    INET4        NOT NULL UNIQUE,
    creation_date DATETIME(3)  NOT NULL DEFAULT UTC_TIMESTAMP(3)

    CONSTRAINT fk_ip_blacklist_staff FOREIGN KEY(staff_id) REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS role(

    id       INT UNSIGNED     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name     TINYTEXT         NOT NULL,
    -- The order of which the roles are displayed, smallest first.
    priority TINYINT UNSIGNED NOT NULL,
    symbol   VARCHAR(16)      NOT NULL,
    color    CHAR(8)          NOT NULL
);

CREATE TABLE IF NOT EXISTS permission(

    id       INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    -- Lowercase with - as space separators.
    property TINYTEXT     NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS account_role(

    id         INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id INT UNSIGNED NOT NULL,
    role_id    INT UNSIGNED NOT NULL,

    CONSTRAINT fk_roles_user FOREIGN KEY(account_id) REFERENCES account(id) ON DELETE CASCADE,
    CONSTRAINT fk_roles_role FOREIGN KEY(role_id)    REFERENCES role(id)    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS role_permission(

    id            INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_id       INT UNSIGNED NOT NULL,
    permission_id INT UNSIGNED NOT NULL,

    CONSTRAINT fk_role       FOREIGN KEY(role_id)       REFERENCES role(id)       ON DELETE CASCADE,
    CONSTRAINT fk_permission FOREIGN KEY(permission_id) REFERENCES permission(id) ON DELETE CASCADE
);
