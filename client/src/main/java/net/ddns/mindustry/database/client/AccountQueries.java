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
    LoginStatus login(String username, String password, String ip, String uuid, int durationHours) throws DataAccessException;

    void logout(Account account) throws DataAccessException;

    JoinStatus joinsServer(Account account, Server server) throws DataAccessException;

    void leavesServer(Account account) throws DataAccessException;

    sealed interface LoginStatus {
        record LoggedIn(Account account) implements LoginStatus {}
        record WrongCredentials() implements LoginStatus  {}
        record AlreadyLoggedIn() implements LoginStatus {}
    }

    enum JoinStatus {
        JOINED,
        ALREADY_IN_SERVER,
        SESSION_EXPIRED,
        NOT_AUTHENTICATED
    }
}
