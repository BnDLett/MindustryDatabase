package net.ddns.mindustry.database.client;

import net.ddns.mindustry.database.schema.tables.pojos.Account;
import net.ddns.mindustry.database.schema.tables.pojos.Server;
import org.jooq.exception.DataAccessException;
import java.util.Optional;

public interface AccountQueries {

    /// Searches the account with this username.
    /// @return the account if found.
    Optional<Account> find(String username) throws DataAccessException;

    /// Does a login attempt and if successful, creates a new session with the provided duration.
    /// @param username the username of the account.
    /// @param password the password of the account.
    /// @param ip the ip of player used
    /// @param durationHours the duration of the session in hours.
    LoginStatus login(String username, char[] password, String ip, String uuid, int durationHours) throws DataAccessException;

    void logout(Account account) throws DataAccessException;

    JoinStatus joinsServer(Server server, String ip, String uuid) throws DataAccessException;

    void leavesServer(Account account) throws DataAccessException;

    sealed interface LoginStatus {

        /// The credentials are correct and the account has logged in.
        record LoggedIn(Account account) implements LoginStatus {}

        /// The username or password provided are wrong.
        record WrongCredentials() implements LoginStatus {}

        /// The account is already logged in and does not require authentication.
        record AlreadyLoggedIn() implements LoginStatus {}
    }

    sealed interface JoinStatus {

        /// The account is authenticated and joined in the server.
        record Joined(Account account) implements JoinStatus {}

        /// The account is already connected in this or another server.
        record AlreadyInServer() implements JoinStatus {}

        /// The account session has expired, and the account must re-authenticate.
        record SessionExpired() implements JoinStatus {}

        /// The account is not authenticated.
        record NotAuthenticated() implements JoinStatus {}

        /// The account does not have enough authorizations to join this server.
        record NotAuthorized() implements JoinStatus {}
    }
}
