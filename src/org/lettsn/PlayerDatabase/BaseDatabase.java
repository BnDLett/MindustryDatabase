package org.lettsn.PlayerDatabase;

import arc.util.Log;
import mindustry.gen.Player;
import mindustry.net.Administration;

import java.sql.*;
import java.util.Random;

public class BaseDatabase {
    Connection databaseConnection;
    final Random randomGenerator = new Random();

    public BaseDatabase() throws SQLException {
        Connection connection;

        Administration.Config databaseURL = new Administration.Config("db-url",
                "The URL of the database.", "");
        Administration.Config databaseLocation = new Administration.Config("db-name",
                "The name of the database.", "mindustry");
        Administration.Config databaseUsername = new Administration.Config("db-username",
                "The username for the moderation database.", "");
        Administration.Config databasePassword = new Administration.Config("db-password",
                "The password for the moderation database.", "");

        if (databaseURL.string().strip().isEmpty()) {
            Log.warn("The URL configuration for the database is empty.");
            return;
        }

        try {
            Class.forName ("org.mariadb.jdbc.Driver");
            String url = String.format("jdbc:mariadb://%s/%s", databaseURL.string(), databaseLocation.string());
            connection = DriverManager.getConnection(url, databaseUsername.string(), databasePassword.string());
        } catch ( Exception e ) {
            Log.err(e.getClass().getName() + ": " + e.getMessage());
            return;
        }

        this.databaseConnection = connection;
        Statement statement = connection.createStatement();

        // TODO: UPDATE README TABLE

        statement.executeUpdate(" " +
                "CREATE TABLE IF NOT EXISTS players (" +
                "   permanent_id   SERIAL          PRIMARY KEY,     " +
                "   uuid           TEXT,                            " +
                "   session_id     INT,                             " +
                "   time_active    BIGINT UNSIGNED,                 " +
                "   is_registered  TINYINT(1)                       " +
                ");"
        );

        statement.close();
    }

    // --- PLAYERS ---

    /**
     * Adds a player to the database. Will NOT throw an error if the player is already in the database.
     * @param player The player to add to the database.
     */
    public void addPlayer(Player player) {
        PreparedStatement preparedStatement;

        try {
            preparedStatement = this.databaseConnection.prepareStatement(
                        """
                        INSERT IGNORE INTO players (uuid, time_active, is_registered) VALUES (
                            ?,
                            ?,
                            ?
                        );
                        """
            );

            preparedStatement.setString(1, player.uuid());
            preparedStatement.setInt(2, 0);
            preparedStatement.setBoolean(3, false);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException sqlException) {
            Log.debug(sqlException);
            Log.err("COULD NOT ADD PLAYER TO DATABASE.");
        }
    }

    /**
     * Gets a player from the database using a player object.
     * @param player The player object.
     */
    public void getPlayer(Player player) {
        PreparedStatement preparedStatement;

        try {
            preparedStatement = this.databaseConnection.prepareStatement(
                    """
                    INSERT IGNORE INTO players (uuid, time_active, is_registered) VALUES (
                        ?,
                        ?,
                        ?
                    );
                    """
            );

            preparedStatement.setString(1, player.uuid());
            preparedStatement.setInt(2, 0);
            preparedStatement.setBoolean(3, false);

            preparedStatement.executeQuery();
            preparedStatement.close();
        } catch (SQLException sqlException) {
            Log.debug(sqlException);
            Log.err("COULD NOT RETRIEVE PLAYER FROM DATABASE.");
        }
    }

    /**
     * Gets a player from the database using a player's permanent ID.
     * @param permanentID The permanent ID of the player.
     */
    public void getPlayer(int permanentID) {

    }

    public void removePlayer(int permanentID) {

    }

    public <T> void genericUpdate(int permanentID, T value) {
        PreparedStatement preparedStatement;

        try {
            preparedStatement = this.databaseConnection.prepareStatement(
                    """
                    UPDATE players SET session_id = ? WHERE permanent_id = ?
                    """
            );

            preparedStatement.setString(1, value);
            preparedStatement.setInt(2, permanentID);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException sqlException) {
            Log.debug(sqlException);
            Log.err("COULD NOT ASSIGN SESSION ID TO PLAYER.");
        }
    }

    // --- PLAYER SESSION ID ---

    /**
     * Updates a player's session ID to the specified ID hex.
     * @param permanentID The permanent ID of the player.
     * @param sessionIDHex The session ID hex.
     */
    public void updateSessionID(int permanentID, String sessionIDHex) {
        PreparedStatement preparedStatement;

        try {
            preparedStatement = this.databaseConnection.prepareStatement(
                    """
                    UPDATE players SET session_id = ? WHERE permanent_id = ?
                    """
            );

            preparedStatement.setString(1, sessionIDHex);
            preparedStatement.setInt(2, permanentID);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException sqlException) {
            Log.debug(sqlException);
            Log.err("COULD NOT ASSIGN SESSION ID TO PLAYER.");
        }
    }

    /**
     * Adds a session ID to a player. Will automatically generate a hex ID.
     * @param permanentID The permanent ID of the player.
     */
    public void addSessionID(int permanentID) {
        long newPlayerID = this.randomGenerator.nextInt() + (1L << 31);
        String hexPlayerID = Long.toHexString(newPlayerID);

        updateSessionID(permanentID, hexPlayerID);
    }

    /**
     * Removes a session ID from a player.
     * @param permanentID The permanent ID of the player.
     */
    public void removeSessionID(int permanentID) {
        updateSessionID(permanentID, null);
    }

    // --- STAFF ---

    public void addStaff() {

    }

    public void getStaff() {

    }

    public void removeStaff() {

    }

    // --- BANS ---

    public void addBan() {

    }

    public void getBan() {

    }

    public void removeBan() {

    }

    // --- KICKS ---

    public void addKick() {

    }

    public void getKick() {

    }

    public void removeKick() {

    }

    // --- WARNS ---

    public void addWarn() {

    }

    public void getWarn() {

    }

    public void removeWarn() {

    }

    // --- NAMES ---

    public void addName() {

    }

    public void getName() {

    }

    public void removeName() {

    }

    // --- IP ADDRESSES ---

    public void addIP() {

    }

    public void getIP () {

    }

    public void removeIP () {

    }
}
