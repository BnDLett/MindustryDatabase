package net.ddns.mindustry.database.client.impl;

import net.ddns.mindustry.database.client.AccountQueries;
import net.ddns.mindustry.database.schema.tables.pojos.Account;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.types.UInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.util.Objects;
import java.util.Optional;
import static net.ddns.mindustry.database.schema.Tables.*;

public record AccountQueriesImpl(DSLContext dsl) implements AccountQueries {

    private static final MessageDigest SHA2_256;

    static {
        try { SHA2_256 = MessageDigest.getInstance("SHA2-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find the wanted algorithm.", e);
        }
    }

    public Optional<Account> find(DSLContext tDsl, String username) {
        Objects.requireNonNull(username);
        return tDsl.selectFrom(ACCOUNT)
                .where(ACCOUNT.USERNAME.eq(username.strip().toLowerCase()))
                .fetchOptionalInto(Account.class);
    }

    @Override
    public Optional<Account> find(String username) {
        return find(dsl, username);
    }

    @Override
    public boolean extendSession(String ip, String uuid, int hours) throws DataAccessException {

        if (hours <= 0) throw new IllegalArgumentException("The hours cannot be negative or 0.");
        final byte[] session = SHA2_256.digest((Objects.requireNonNull(ip) + Objects.requireNonNull(uuid))
                .getBytes(StandardCharsets.UTF_8));

        return dsl.transactionResult(cfx -> {
            final DSLContext tDsl = cfx.dsl();

            final var result = tDsl.select(ACCOUNT_SESSION.ID, ACCOUNT_SESSION.EXPIRATION_DATE)
                    .from(ACCOUNT_SESSION)
                    .where(ACCOUNT_SESSION.SESSION_COOKIE.eq(session))
                    .fetchOne();

            if (result == null) return false; // No session available.

            final UInteger id = result.value1();
            final OffsetDateTime expiration = result.value2().atOffset(ZoneOffset.UTC);
            final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

            // I check if the session expired if so I remove the row.
            if (expiration.isBefore(now)) {
                tDsl.deleteFrom(ACCOUNT_SESSION)
                        .where(ACCOUNT_SESSION.ID.eq(id))
                        .execute();
                return false;
            }

            tDsl.update(ACCOUNT_SESSION)
                    .set(ACCOUNT_SESSION.EXPIRATION_DATE, now.plusHours(hours).toLocalDateTime())
                    .where(ACCOUNT_SESSION.ID.eq(id))
                    .execute();

            return true;
        });
    }
}
