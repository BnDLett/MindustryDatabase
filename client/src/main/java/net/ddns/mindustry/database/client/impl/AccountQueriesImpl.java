package net.ddns.mindustry.database.client.impl;

import net.ddns.mindustry.database.client.AccountQueries;
import net.ddns.mindustry.database.client.SecurityConfig;
import net.ddns.mindustry.database.schema.tables.pojos.Account;
import net.ddns.mindustry.database.schema.tables.pojos.Server;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.types.UInteger;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Objects;
import java.util.Optional;
import static net.ddns.mindustry.database.schema.Tables.*;

public record AccountQueriesImpl(DSLContext dsl, SecurityConfig security) implements AccountQueries {

    private byte[] createSessionHash(String ip, String uuid) {
        Objects.requireNonNull(ip);
        Objects.requireNonNull(uuid);
        return security().sessionHash().digest((ip + uuid).getBytes(StandardCharsets.UTF_8));
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

    public Account find(DSLContext tDsl, UInteger id) {
        return tDsl.selectFrom(ACCOUNT)
                .where(ACCOUNT.ID.eq(id))
                .fetchOneInto(Account.class);
    }

    @Override
    public Optional<Account> find(String username) {
        return find(dsl, username);
    }

    @Override
    public LoginStatus login(String username, char[] password, String ip, String uuid, int durationHours) {

        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        if (durationHours <= 0) throw new IllegalArgumentException("The hours cannot be negative or 0.");

        final byte[] session = createSessionHash(ip, uuid);

        return dsl.transactionResult(ctx -> {
            DSLContext tDsl = ctx.dsl();

            if (hasSession(tDsl, session)) return new LoginStatus.AlreadyLoggedIn();

            /*
            I calculate the compute-expensive password.
            I do this here because:
             - I want the already authenticated players to be checked fast.
             - I want to take the same amount of time if the user exists or not, to avoid username scanning.
             */
            final String hashedPassword = security().hashPass(password);

            final Account account = find(tDsl, username).orElse(null);
            if (account == null) return new LoginStatus.WrongCredentials();

            if (!security.verifyPassHash(hashedPassword, password)) return new LoginStatus.WrongCredentials();

            // I update the password in case the argon2 settings have been changed.
            tDsl.update(ACCOUNT)
                    .set(ACCOUNT.PASSWORD, hashedPassword)
                    .where(ACCOUNT.ID.eq(account.id()))
                    .execute();

            // I create a new session for the user.
            createSession(tDsl, account.id(), session, durationHours);

            return new LoginStatus.LoggedIn(account);
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
    public JoinStatus joinsServer(Server server, String ip, String uuid) throws DataAccessException {

        Objects.requireNonNull(server);
        final byte[] session = createSessionHash(ip, uuid);

        return dsl.transactionResult(ctx -> {
            final DSLContext tDsl = ctx.dsl();

            final var result = tDsl.select(ACCOUNT_SESSION.ACCOUNT_ID, ACCOUNT_SESSION.EXPIRATION_DATE)
                    .from(ACCOUNT_SESSION)
                    .where(ACCOUNT_SESSION.SESSION_COOKIE.eq(session))
                    .fetchOne();

            // No entry means not authenticated.
            if (result == null) return new JoinStatus.NotAuthenticated();

            final UInteger accountId = result.get(ACCOUNT_SESSION.ACCOUNT_ID);

            // I check if the session expired.
            if (result.get(ACCOUNT_SESSION.EXPIRATION_DATE)
                    .atOffset(ZoneOffset.UTC)
                    .isBefore(OffsetDateTime.now(ZoneOffset.UTC))) return new JoinStatus.SessionExpired();

            // I check if the account is already inside a server.
            if (tDsl.selectOne()
                    .from(SERVER_JOIN)
                    .where(SERVER_JOIN.ACCOUNT_ID.eq(accountId).and(SERVER_JOIN.LEAVE_DATE.isNull()))
                    .fetchOptional()
                    .isPresent()) return new JoinStatus.AlreadyInServer();

            tDsl.insertInto(SERVER_JOIN)
                    .set(SERVER_JOIN.ACCOUNT_ID, accountId)
                    .set(SERVER_JOIN.SERVER_ID, server.id())
                    .execute();

            // TODO Server authorization.

            return new JoinStatus.Joined(find(tDsl, accountId));
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
