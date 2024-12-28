# Mindustry Database
A Mindustry plugin for saving and handling data about players.

[![](https://jitpack.io/v/BnDLett/moderation_system.svg)](https://jitpack.io/#BnDLett/moderation_system)

# Keep in mind
This is, in essence, a branch of Lett's moderation system. However, instead of
bundling the moderation system and database into one package, it'll just be
the database on its own. This is to help encourage modularity and allow others
to build off an already-designed database. Additionally, keep in mind that
this is designed with the interests of mindustry.ddns.net in consideration.

This won't provide any features or functionality beyond storing and saving
player data.

# Installation
## Supports
[ ] Client <br>
[ ] Client server <br>
[x] Headless server

## Headless Server
1. Download the latest `.jar`
2. Copy the `.jar`
3. Navigate to your server root directory
4. Navigate to `[server root]/config/mods`
5. Paste the `.jar`
6. Start or restart the server

# Database schema
## Tables
### players

| Column Name    | Data Type       | Key       |
|----------------|-----------------|-----------|
| permanent_id   | SERIAL          | PRIMARY   | 
| uuid           | TEXT            |           |
| session_id     | SERIAL          |           |
| time_active    | BIGINT UNSIGNED |           |
| is_registered  | TINYINT(1)      |           |

### staff

| Column Name  | Data Type      | Key      |
|--------------|----------------|----------|
| discord_id   | INT UNSIGNED   | PRIMARY  |
| admin        | TINYINT(1)     |          |
| start_time   | TIMESTAMP      |          |
| time_active  | DATETIME       |          |
| permanent_id | INT            | FOREIGN  | 

### bans

| Column Name  | Data Type | Key     |
|--------------|-----------|---------|
| ban_id       | SERIAL    | PRIMARY |
| ban_reason   | TEXT      |         |
| ban_start    | TIMESTAMP |         |
| ban_end      | DATETIME  |         |
| banned_by    | INT       |         |
| permanent_id | INT       | FOREIGN |

### kicks

| Column Name  | Data Type | Key      |
|--------------|-----------|----------|
| kick_id      | SERIAL    | PRIMARY  |
| kick_reason  | TEXT      |          |
| created_at   | TIMESTAMP |          |
| kicked_by    | INT       |          |
| permanent_id | INT       | FOREIGN  |

### warns

| Column Name  | Data Type | Key       |
|--------------|-----------|-----------|
| warn_id      | SERIAL    | PRIMARY   |
| warn_reason  | TEXT      |           |
| created_at   | TIMESTAMP |           |
| warned_by    | INT       |           |
| permanent_id | INT       | FOREIGN   |

### names

| Column Name  | Data Type | Key     |
|--------------|-----------|---------|
| name_id      | SERIAL    | PRIMARY |
| name         | TEXT      |         |
| created_at   | TIMESTAMP |         |
| permanent_id | INT       | FOREIGN |

### ip_addresses

| Column Name  | Data Type  | Key      |
|--------------|------------|----------|
| ip_id        | SERIAL     | PRIMARY  |
| ip           | INET6      |          |
| created_at   | TIMESTAMP  |          |
| permanent_id | INT        | FOREIGN  |

## Side-note
The above markdown tables were generated with Gemini 1.5 Flash.
