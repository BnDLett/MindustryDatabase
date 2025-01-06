package net.ddns.mindustry.database.client;

import net.ddns.mindustry.database.schema.tables.pojos.Account;
import org.jooq.exception.DataAccessException;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface AccountQueries {

    /// Searches the account with this username.
    /// @return the account if found.
    Optional<Account> find(String username);

    /// Extends the account session expiration date if authenticated.
    /// @param ip the ip address of the account.
    /// @param uuid the mindustry uuid of the account.
    /// @param hours how many hours to extend the expiration from {@link OffsetDateTime#now() now}.
    /// @return true if the account is authenticated, false if the account needs authentication.
    boolean extendSession(String ip, String uuid, int hours) throws DataAccessException;
}
