package moderation_system;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Administration;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static java.lang.Integer.parseInt;

public class Main extends Plugin {
    public final String pluginMessageName = "[gray]<[#003ec8]Moderation[gray]>[white] ";
    private final Map<String, String> playerIdentifiers = new HashMap<>();
    private final Random randomGenerator = new Random();
    private boolean databaseConfigured;
    private PlayerDatabase database;
    public static Administration.Config databaseLocation;

    /**
     * @param adminLevel whether to only allow up to admin level.
     * @return A boolean representing the player's permission to execute the command.
     */
    public Boolean checkPermission(Boolean adminLevel, String UUID) throws SQLException {
        Player player = database.getPlayerFromUUID(UUID);
        Boolean hasDatabaseAdmin = database.hasAdminPermission(player);

        if (hasDatabaseAdmin == null) {
            return false;
        }

        Log.info(hasDatabaseAdmin);
        return hasDatabaseAdmin || !adminLevel;
    }

    // https://stackoverflow.com/a/2904266
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void databaseNotConfiguredWarning() {
        databaseConfigured = false;
        Log.warn("There is no configuration for the database! Ensure you've set it up so that players" +
                " can join.");
    }

    private void setupDatabase() {
        try {
            database = new PlayerDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(){
        databaseLocation = new Administration.Config("db-location",
                "The location of the MDN Moderation database.", "");

        if (databaseLocation.string().isEmpty()) {
            databaseNotConfiguredWarning();
        }

        if (!databaseConfigured) {
            databaseConfigured = true;
            setupDatabase();
        }

        Events.on(EventType.PlayerJoin.class, event -> {
            if (databaseLocation.string().isEmpty()) {
                databaseNotConfiguredWarning();
                event.player.kick("The moderation system was not properly configured. In order to join the" +
                        " server, the moderation system must first be configured. Please contact a server administrator" +
                        " to address this issue.", 0);
                return;
            }

            long newPlayerID = randomGenerator.nextInt() + (1L << 31);
            String hexPlayerID = Long.toHexString(newPlayerID);
            boolean banned;

//            event.player.sendMessage(pluginMessageName + hexPlayerID);
            playerIdentifiers.put(event.player.uuid(), hexPlayerID);

            try {
                database.addPlayer(event.player.uuid(), event.player.ip(), event.player.name());
                banned = database.checkBan(event.player.uuid());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (banned) {
                String banReason;
                String banID;

                try {
                    banReason = database.getBanReason(event.player.uuid());
                    banID = database.getBanID(event.player.uuid());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                String banMessage = String.format("""
                        [scarlet]You are banned from this server.
                        [orange]Reason[gray]:[white] %s
                        
                        [orange]Ban ID[gray]:[white] %s""", banReason, banID
                );
                event.player.kick(banMessage, 0);
            }
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("kick", "<id> <reason...>", "Kick a player.", (args, player) -> {
            String id = args[0];
            String reason = args[1];
            String playerUUID = getKeyByValue(playerIdentifiers, id);

            try {
                if (!checkPermission(false, player.uuid())) {
                    player.sendMessage(pluginMessageName + "You do not have permission to run this command.");
                    return;
                }
            } catch (SQLException e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                return;
            }

            Player playerToKick = Groups.player.find(p -> p.uuid().equals(playerUUID));

            if(playerToKick == null){
                player.sendMessage(pluginMessageName + "[scarlet]Could not find a player by that ID.");
                return;
            }

            playerToKick.kick(reason);
        });

        handler.<Player>register("warn", "<id> <reason...>", "Warn a player.", (args, player) -> {
            String id = args[0];
            String reason = args[1];
            String playerUUID = getKeyByValue(playerIdentifiers, id);

            try {
                if (!checkPermission(false, player.uuid())) {
                    player.sendMessage(pluginMessageName + "You do not have permission to run this command.");
                    return;
                }
            } catch (SQLException e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                return;
            }

            Player playerToWarn = Groups.player.find(p -> p.uuid().equals(playerUUID));

            if(playerToWarn == null){
                player.sendMessage(pluginMessageName + "[scarlet]Could not find a player by that ID.");
                return;
            }

            player.sendMessage(pluginMessageName + "[scarlet]WARNING[gray]:[white] " + reason);
        });

        handler.<Player>register("info", "<username...>", "Get the info of a player.", (args, player) -> {
            String username = args[0];

            try {
                if (!checkPermission(false, player.uuid())) {
                    player.sendMessage(pluginMessageName + "You do not have permission to run this command.");
                    return;
                }
            } catch (SQLException e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                return;
            }

            Player targetPlayer = Groups.player.find(p -> p.plainName().equalsIgnoreCase(username));

            if (targetPlayer == null) {
                player.sendMessage(pluginMessageName + "[scarlet]Could not find a player by that username.");
                return;
            }

            String targetPlayerID = playerIdentifiers.get(targetPlayer.uuid());

            String infoMessage = String.format("""
                    ID: %s
                    IP: %s
                    Last Name Used: %s[white]
                    Is admin: %b
                    """,
                    targetPlayerID,
                    targetPlayer.ip(),
                    targetPlayer.name(),
                    targetPlayer.admin()
            );
            player.sendMessage(infoMessage);
        });

        handler.<Player>register("ban", "<id> <duration> <reason...>", "Ban a player.",
                (args, player) -> {
            String id = args[0];
            long duration = parseInt(args[1]);
            String reason = args[2];

            long durationMillis = duration * 86400000;

            long currentTime = System.currentTimeMillis();
            long endTime = currentTime + durationMillis;

            try {
                if (!checkPermission(true, player.uuid())) {
                    player.sendMessage(pluginMessageName + "You do not have permission to run this command.");
                    return;
                }
            } catch (SQLException e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                return;
            }

            String staffID;
            try {
                staffID = database.getStaffID(player.uuid());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            String playerUUID = getKeyByValue(playerIdentifiers, id);
            Player playerToBan = Groups.player.find(p -> p.uuid().equals(playerUUID));

            if (playerToBan == null) {
                player.sendMessage(pluginMessageName + "[scarlet]Could not find a player by that ID.");
                return;
            }

            try {
                database.addBan(playerUUID, reason, String.valueOf(endTime), staffID);
                String banID = database.getBanID(playerUUID);

                String banMessage = String.format("""
                        [scarlet]You are banned from this server.
                        [orange]Reason[gray]:[white] %s
                        
                        [orange]Ban ID[gray]:[white] %s""", reason, banID
                );
                playerToBan.kick(banMessage, 0);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("add_staff", "<uuid> <discord_id> <admin>", "Adds a new staff member. Set" +
                " admin to true if they have the ability to ban and unban.", args -> {
            String UUID = args[0];
            String discordID = args[1];
            boolean admin = args[2].equals("true") || args[2].equals("1");

            try {
                database.addStaff(UUID, admin, discordID);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            Log.info("Staff added.");
        });
    }
}