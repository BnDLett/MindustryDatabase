package moderation_system;

import mindustry.gen.Groups;
import mindustry.gen.Player;

import java.sql.*;

public class DDNSPlayerDatabase {
    Connection databaseConnection;
    public DDNSPlayerDatabase() throws SQLException {
        Connection connection = null;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:config/test.db");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return;
        }

        this.databaseConnection = connection;
        Statement statement = connection.createStatement();

        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players
                (
                uuid        TEXT  PRIMARY KEY  NOT NULL,
                last_ip     TEXT               NOT NULL,
                last_name   TEXT               NOT NULL,
                )
                """);
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS banned_players
                (
                FOREIGN KEY (uuid) REFERENCES players(uuid)
                ban_reason  TEXT               NOT NULL,
                ban_start   INT                NOT NULL,
                ban_end     INT                NOT NULL,
                )
                """);
        statement.close();
    }

    public Player getPlayerFromBanID(String banID) throws SQLException {
        Statement statement = this.databaseConnection.createStatement();
        ResultSet databaseResult = statement.executeQuery(String.format("""
                SELECT * WHERE ban_id == %s FROM banned_players
                """, banID
        ));

        String uuid = databaseResult.getString("uuid");
        System.out.println(uuid);

        statement.close();

        Player player = Groups.player.find(p -> p.uuid().equals(uuid));
        return player;
    }

    public Player getPlayerFromUUID(String UUID) throws SQLException {
        Statement statement = this.databaseConnection.createStatement();
        ResultSet databaseResult = statement.executeQuery(String.format("""
                SELECT * WHERE uuid == %s FROM players
                """, UUID
        ));

        String uuid = databaseResult.getString("uuid");
        System.out.println(uuid);

        statement.close();

        Player player = Groups.player.find(p -> p.uuid().equals(uuid));
        return player;
    }

    public void addPlayer(String UUID, String lastIP, String lastName) throws SQLException {
        Statement statement = this.databaseConnection.createStatement();

        Player player = this.getPlayerFromUUID(UUID);
        if (player != null) {
            return;
        }

        statement.executeUpdate(String.format("""
                INSERT INTO players (uuid, last_ip, last_name) VALUES (%s, %s, %s);
                """, UUID, lastIP, lastName
        ));
    }
}
