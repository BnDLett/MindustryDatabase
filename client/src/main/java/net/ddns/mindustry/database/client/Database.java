package net.ddns.mindustry.database.client;

import net.ddns.mindustry.database.client.impl.DatabaseImpl;

public interface Database {

    static Database newConnection(String url, String user, String password) {
        return new DatabaseImpl(url, user, password);
    }

    AccountQueries auth();


}
