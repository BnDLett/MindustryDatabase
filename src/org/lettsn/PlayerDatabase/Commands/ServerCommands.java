package org.lettsn.PlayerDatabase.Commands;

import arc.util.CommandHandler;
import arc.util.Log;
import org.lettsn.PlayerDatabase.BaseDatabase;
import org.lettsn.PlayerDatabase.Main;

import java.sql.SQLException;

public class ServerCommands {
    public static void load(CommandHandler handler) {
        handler.register(
                "reconnect",
                "Reconnects the database plugin to the MariaDB server.",
                ServerCommands::reconnect
        );
    }

    public static void reconnect(String[] args) {
        try {
            Main.baseDatabase = new BaseDatabase();
        } catch (SQLException e) {
            Log.err(e);
        }
    }
}
