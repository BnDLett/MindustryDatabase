package org.lettsn.PlayerDatabase;

import arc.util.Log;
import mindustry.net.Administration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    Connection databaseConnection;

    public void database() throws SQLException {
        Connection connection;

        Administration.Config databaseURL = new Administration.Config("db-url",
                "The URL of the database.", "");
        Administration.Config databaseUsername = new Administration.Config("db-username",
                "The username for the moderation database.", "");
        Administration.Config databasePassword = new Administration.Config("db-password",
                "The password for the moderation database.", "");

        if (databaseURL.string().strip().isEmpty()) {
            Log.warn("The URL configuration for the database is empty.");
            return;
        }

        try {
            Class.forName("org.mariadb.JDBC.Driver");
            String url = String.format("jdbc:mariadb:%s", databaseURL.string());
            connection = DriverManager.getConnection(url, databaseUsername.string(), databasePassword.string());
        } catch ( Exception e ) {
            Log.err(e.getClass().getName() + ": " + e.getMessage());
            return;
        }

        this.databaseConnection = connection;
        Statement statement = connection.createStatement();

        // TODO: add tables

        statement.close();
    }
}
