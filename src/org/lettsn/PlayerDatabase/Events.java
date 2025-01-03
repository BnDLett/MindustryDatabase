package org.lettsn.PlayerDatabase;

import static org.lettsn.PlayerDatabase.Main.baseDatabase;

/**
 * This class handles when an event within this project is triggered.
 */
public class Events {
    public static void load() {
        arc.Events.on(mindustry.game.EventType.PlayerJoin.class, e -> {
            baseDatabase.addPlayer(e.player);
            baseDatabase.addSessionID();

            arc.Events.fire(new EventType.PlayerAddedToDatabase(e.player));
        });
    }
}
