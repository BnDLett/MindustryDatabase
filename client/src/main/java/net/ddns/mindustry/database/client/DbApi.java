package net.ddns.mindustry.database.client;

import net.ddns.mindustry.database.client.impl.DbImpl;
import net.ddns.mindustry.database.schema.MindustryDatabase;

import java.net.URI;

// TODO
public interface DbApi {

    static DbApi create() {


        return new DbImpl(URI.create("jdbc:mariadb://localhost:5432/placeholder"), "username", "password");
    }


}
