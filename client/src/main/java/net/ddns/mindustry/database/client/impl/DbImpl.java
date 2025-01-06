package net.ddns.mindustry.database.client.impl;

import net.ddns.mindustry.database.client.DbApi;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import java.net.URI;

public final class DbImpl implements DbApi {

    private final DSLContext dsl;

    public DbImpl(URI uri, String username, String password) {
        this.dsl = DSL.using(uri.toString(), username, password);
    }




}
