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
This table stores information about players.

| Column        | Type            | Constraints | Description                                                                     |
|---------------|-----------------|-------------|---------------------------------------------------------------------------------|
| uuid          | TEXT            | PRIMARY KEY | The UUID for the player.                                                        |
| latest_ip     | TEXT            |             | The player's most recent IP address.                                            |
| latest_name   | TEXT            |             | The player's most recent in-game name.                                          |
| permanent_id  | SERIAL          |             | A unique, auto-incrementing ID for each player.                                 |
| session_id    | TEXT            |             | An identifier for the player's current or last session.                         |
| time_active   | BIGINT UNSIGNED |             | Total time the player has been active (in seconds).                             |
| is_registered | TINYINT(1)      |             | Indicates whether the player is registered (e.g., on a website or forum). (0/1) |

### staff
This table stores information about staff members.

| Column       | Type            | Constraints | Description                                                                        |
|--------------|-----------------|-------------|------------------------------------------------------------------------------------|
| discord_id   | INT UNSIGNED    | PRIMARY KEY | Discord ID of the staff member.                                                    |
| admin        | TINYINT(1)      |             | Indicates if the staff member has admin privileges (0/1).                          |
| start_time   | BIGINT UNSIGNED |             | Timestamp of when the staff member joined the staff team (in seconds since epoch). |
| time_active  | BIGINT UNSIGNED |             | Total time the staff member has been active (in seconds).                          |
| uuid         | TEXT            | FOREIGN KEY | UUID of the staff member (if they are also a player).                              |
| permanent_id | SERIAL          | FOREIGN KEY | Permanent ID of the staff member (if they are also a player).                      |

### bans
This table stores information about player bans.

| Column           | Type            | Constraints | Description                                                    |
|------------------|-----------------|-------------|----------------------------------------------------------------|
| ban_id           | SERIAL          | PRIMARY KEY | Unique identifier for each ban.                                |
| ban_reason       | TEXT            |             | The reason for the ban.                                        |
| ban_start        | BIGINT UNSIGNED |             | Timestamp of when the ban started (in seconds since epoch).    |
| ban_end          | BIGINT UNSIGNED |             | Timestamp of when the ban ends (0 or null for permanent bans). |
| staff_discord_id | INT UNSIGNED    | FOREIGN KEY | Discord ID of the staff member who issued the ban.             |
| staff_id         | SERIAL          | FOREIGN KEY | The permanent ID of the staff member that issued the ban.      |
| uuid             | TEXT            | FOREIGN KEY | UUID of the banned player.                                     |
| latest_name      | TEXT            | FOREIGN KEY | Last known name of the banned player.                          |
| permanent_id     | SERIAL          | FOREIGN KEY | Permanent ID of the banned player.                             |

### names
This table stores all names used in the server (including color tags).

| Column       | Type   | Constraints | Description                                      |
|--------------|--------|-------------|--------------------------------------------------|
| name         | TEXT   |             | A player's in-game name.                         |
| permanent_id | SERIAL | FOREIGN KEY | Permanent ID of the player this name belongs to. |
| uuid         | TEXT   | FOREIGN KEY | UUID of the player this name belongs to.         |

### kicks
This table stores information about kicks.

| Column       | Type   | Constraints | Description                           |
|--------------|--------|-------------|---------------------------------------|
| kick_reason  | TEXT   |             | The reason for the kick.              |
| latest_name  | TEXT   | FOREIGN KEY | Last known name of the kicked player. |
| permanent_id | SERIAL | FOREIGN KEY | Permanent ID of the kicked player.    |
| uuid         | TEXT   | FOREIGN KEY | UUID of the kicked player.            |

### warns
This table stores information about warnings.

| Column        | Type   | Constraints | Description                           |
|---------------|--------|-------------|---------------------------------------|
| warn_reason   | TEXT   |             | The reason for the warning.           |
| latest_name   | TEXT   | FOREIGN KEY | Last known name of the warned player. |
| permanent_id  | SERIAL | FOREIGN KEY | Permanent ID of the warned player.    |
| uuid          | TEXT   | FOREIGN KEY | UUID of the warned player.            |

## Side-note
I generated all these with AI. All I did was use an image of the database
schema that was made in LucidChart. Programmers are so fucking cooked.

In all seriousness though, AI is great as a tool -- don't use it to straight 
up generate code for your project, though. Gemini 2.0 Flash (experimental) 
was the AI model used as a tool to generate the database tables in markdown.
Of course, with AI being AI, I did add a few modifications -- but it was
accurate enough to the point where the modifications were fairly minimal.
