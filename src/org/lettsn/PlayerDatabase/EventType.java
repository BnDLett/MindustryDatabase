package org.lettsn.PlayerDatabase;

import mindustry.gen.*;

/**
 * This class contains the Events that are related to this project and the database.
 */
public class EventType {
    public static class PlayerAddedToDatabase {
        Player player;

        public PlayerAddedToDatabase(Player player) {
            this.player = player;
        }
    }
}
