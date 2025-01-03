package org.lettsn.PlayerDatabase.SQLStructures;

/**
 * A class to hold data of the player data retrieved from the database.
 */
public class SQLPlayer {
    public int permanentID;
    public String uuid;
    public String sessionID;
    public Long timeActive;
    public Boolean isRegistered;

    /**
     * A class to hold data of the player data retrieved from the database.
     *
     * @param permanentID The permanent ID of the player.
     * @param uuid The UUID of the player.
     * @param sessionID The session ID of the player.
     * @param timeActive The amount of time that the player has been active.
     * @param isRegistered Whether the player is registered.
     */
    public SQLPlayer(int permanentID, String uuid, String sessionID, Long timeActive, Boolean isRegistered) {
        this.permanentID = permanentID;
        this.uuid = uuid;
        this.sessionID = sessionID;
        this.timeActive = timeActive;
        this.isRegistered = isRegistered;
    }
}
