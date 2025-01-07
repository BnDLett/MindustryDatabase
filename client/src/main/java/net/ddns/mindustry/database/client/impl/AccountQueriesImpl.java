package net.ddns.mindustry.database.client.impl;

import net.ddns.mindustry.database.client.AccountQueries;
import net.ddns.mindustry.database.schema.tables.pojos.Account;
import net.ddns.mindustry.database.schema.tables.pojos.Server;
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

    private boolean hasSession(DSLContext tDsl, byte[] session) {
        return tDsl.select(ACCOUNT_SESSION.ID, ACCOUNT_SESSION.EXPIRATION_DATE)
                .from(ACCOUNT_SESSION)
                .where(ACCOUNT_SESSION.SESSION_COOKIE.eq(session))
                .fetchOptional()
                .map(result -> result.get(ACCOUNT_SESSION.EXPIRATION_DATE).atOffset(ZoneOffset.UTC))
                // In case the session has not expired, I return true.
                .map(expiration -> expiration.isAfter(OffsetDateTime.now(ZoneOffset.UTC)))
                .orElse(false); // No entry, no session available.
    }

    private void createSession(DSLContext tDsl, UInteger accountId, byte[] session, int durationHours) throws DataAccessException {

        final var expiration = OffsetDateTime.now(ZoneOffset.UTC)
                .plusHours(durationHours)
                .toLocalDateTime();

        tDsl.insertInto(ACCOUNT_SESSION)
                .set(ACCOUNT_SESSION.ACCOUNT_ID, accountId)
                .set(ACCOUNT_SESSION.SESSION_COOKIE, session)
                .set(ACCOUNT_SESSION.EXPIRATION_DATE, expiration)
                .execute();
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
    public LoginStatus login(String username, String password, String ip, String uuid, int durationHours) {

        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        Objects.requireNonNull(ip);
        Objects.requireNonNull(uuid);
        if (durationHours <= 0) throw new IllegalArgumentException("The hours cannot be negative or 0.");

        final byte[] session = SHA2_256.digest((ip + uuid).getBytes(StandardCharsets.UTF_8));

        return dsl.transactionResult(ctx -> {
            DSLContext tDsl = ctx.dsl();

            if (hasSession(tDsl, session)) return new LoginStatus.AlreadyLoggedIn();

            final Account account = find(tDsl, username).orElse(null);
            if (account == null) return new LoginStatus.WrongCredentials();

            // TODO Argon2id password.

            throw new IllegalStateException("Not implemented");
        });
    }

    @Override
    public void logout(Account account) {
        Objects.requireNonNull(account);
        dsl.deleteFrom(ACCOUNT_SESSION)
                .where(ACCOUNT_SESSION.ACCOUNT_ID.eq(account.id()))
                .execute();
    }

    @Override
    public JoinStatus joinsServer(Account account, Server server) throws DataAccessException {

        Objects.requireNonNull(account);
        Objects.requireNonNull(server);

        return dsl.transactionResult(ctx -> {
            final DSLContext tDsl = ctx.dsl();

            final var result = tDsl.select(ACCOUNT_SESSION.EXPIRATION_DATE)
                    .from(ACCOUNT_SESSION)
                    .where(ACCOUNT_SESSION.ACCOUNT_ID.eq(account.id()))
                    .fetchOne();

            // No entry means not authenticated.
            if (result == null) return JoinStatus.NOT_AUTHENTICATED;

            // I check if the session expired.
            if (result.get(ACCOUNT_SESSION.EXPIRATION_DATE)
                    .atOffset(ZoneOffset.UTC)
                    .isBefore(OffsetDateTime.now(ZoneOffset.UTC))) return JoinStatus.SESSION_EXPIRED;

            // I check if the account is already inside a server.
            if (tDsl.selectOne()
                    .from(SERVER_JOIN)
                    .where(SERVER_JOIN.ACCOUNT_ID.eq(account.id()).and(SERVER_JOIN.LEAVE_DATE.isNull()))
                    .fetchOptional()
                    .isPresent()) return JoinStatus.ALREADY_IN_SERVER;

            tDsl.insertInto(SERVER_JOIN)
                    .set(SERVER_JOIN.ACCOUNT_ID, account.id())
                    .set(SERVER_JOIN.SERVER_ID, server.id())
                    .execute();

            return JoinStatus.JOINED;
        });
    }

    @Override
    public void leavesServer(Account account) throws DataAccessException {
        Objects.requireNonNull(account);
        final var now = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        dsl.update(SERVER_JOIN)
                .set(SERVER_JOIN.LEAVE_DATE, now)
                .where(SERVER_JOIN.ACCOUNT_ID.eq(account.id()).and(SERVER_JOIN.LEAVE_DATE.isNull()))
                .execute();
    }
}
