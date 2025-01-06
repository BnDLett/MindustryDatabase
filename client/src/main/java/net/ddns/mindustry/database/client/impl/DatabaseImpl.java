package net.ddns.mindustry.database.client.impl;

import net.ddns.mindustry.database.client.AccountQueries;
import net.ddns.mindustry.database.client.Database;
import org.jooq.impl.DSL;
import java.util.Objects;

public final class DatabaseImpl implements Database {

    private final AccountQueries auth;

    public DatabaseImpl(String url, String username, String password) {
        final var dsl = DSL.using(Objects.requireNonNull(url),
                Objects.requireNonNull(username),
                password);
        this.auth = new AccountQueriesImpl(dsl);
    }

    @Override
    public AccountQueries auth() {
        return auth;
    }
}
