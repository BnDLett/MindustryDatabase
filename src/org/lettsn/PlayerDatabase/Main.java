package org.lettsn.PlayerDatabase;

import arc.util.CommandHandler;
import mindustry.mod.Plugin;

import org.lettsn.PlayerDatabase.Commands.ClientCommands;
import org.lettsn.PlayerDatabase.Commands.ServerCommands;

@SuppressWarnings("unused")
public class Main extends Plugin {
    public static BaseDatabase baseDatabase;

    @Override
    public void init() {
        /* This effectively connects us to the MariaDB server while also handling any SQL exceptions. Since there are no
         * arguments to provide, it'll just be an empty String array.
         */
        ServerCommands.reconnect(new String[0]);

        Events.load();
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        ServerCommands.load(handler);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        ClientCommands.load(handler);
    }
}
